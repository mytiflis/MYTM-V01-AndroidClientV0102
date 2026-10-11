package com.sgc.teknisyen.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sgc.teknisyen.R
import com.sgc.teknisyen.data.remote.ApiService
import com.sgc.teknisyen.data.remote.dto.HeartbeatDto
import com.sgc.teknisyen.data.remote.dto.LocationRequestDto
import com.sgc.teknisyen.data.repository.AuthRepository
import com.sgc.teknisyen.data.repository.SyncRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationService : LifecycleService() {

    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "sgc_location_channel"

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var syncRepository: SyncRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var notificationManager: NotificationManager? = null

    // Coroutines
    private var heartbeatJob: Job? = null
    private var locationRequestJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var lastKnownLocation: Location? = null
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            startForegroundService()
            startLocationUpdates()
            startHeartbeatLoop()
            startLocationRequestPolling()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        stopLocationUpdates()
        heartbeatJob?.cancel()
        locationRequestJob?.cancel()
        coroutineScope.cancel()
        stopForeground(true)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ============================================================
    // FOREGROUND NOTIFICATION
    // ============================================================

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SGC Konum Takibi",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Arka plan konum paylaşımı ve heartbeat"
                enableVibration(false)
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun startForegroundService() {
        val intent = Intent(this, com.sgc.teknisyen.MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.location_service_active))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    // ============================================================
    // LOCATION UPDATES (FusedLocationProvider)
    // ============================================================

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 30_000 // 30 seconds
            fastestInterval = 10_000
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 10f // meters
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                lastKnownLocation = result.lastLocation
                result.locations.forEach { location ->
                    lastKnownLocation = location
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Permission not granted - will be handled by UI
        }
    }

    private fun stopLocationUpdates() {
        locationCallback?.let { callback ->
            fusedLocationClient.removeLocationUpdates(callback)
        }
        locationCallback = null
    }

    // ============================================================
    // HEARTBEAT LOOP (every 60s, or 300s if battery < 15%)
    // ============================================================

    private fun startHeartbeatLoop() {
        heartbeatJob = coroutineScope.launch {
            while (isActive && isRunning) {
                sendHeartbeat()
                val batteryLevel = getBatteryLevel()
                val interval = if (batteryLevel < 15) 300_000L else 60_000L
                delay(interval)
            }
        }
    }

    private suspend fun sendHeartbeat() {
        val technician = authRepository.getCurrentTechnician() ?: return
        val location = lastKnownLocation

        val request = HeartbeatDto.Request.create(
            teknisyenId = technician.id,
            lat = location?.latitude,
            lng = location?.longitude,
            accuracy = location?.accuracy,
            batteryLevel = getBatteryLevel(),
            isCharging = isCharging(),
            networkType = getNetworkType(),
            deviceModel = android.os.Build.MODEL,
            osVersion = android.os.Build.VERSION.RELEASE,
            appVersion = BuildConfig.VERSION_NAME
        )

        try {
            apiService.sendHeartbeat(request)
        } catch (e: Exception) {
            // Heartbeat failed - will retry on next cycle
        }
    }

    private fun getBatteryLevel(): Int {
        // Simplified - in real implementation use BatteryManager
        return 100
    }

    private fun isCharging(): Boolean {
        return false
    }

    private fun getNetworkType(): String {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "offline"
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return "unknown"
        return when {
            caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
                when (telephonyManager.dataNetworkType) {
                    android.telephony.TelephonyManager.NETWORK_TYPE_NR -> "5g"
                    android.telephony.TelephonyManager.NETWORK_TYPE_LTE -> "4g"
                    else -> "cellular"
                }
            }
            else -> "unknown"
        }
    }

    // ============================================================
    // LOCATION REQUEST POLLING (every 15s)
    // ============================================================

    private fun startLocationRequestPolling() {
        locationRequestJob = coroutineScope.launch {
            while (isActive && isRunning) {
                checkPendingLocationRequest()
                delay(15_000)
            }
        }
    }

    private suspend fun checkPendingLocationRequest() {
        val technician = authRepository.getCurrentTechnician() ?: return

        try {
            val response = apiService.getPendingLocationRequest()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                body.istek_id?.let { istekId ->
                    sendLocationForRequest(istekId, technician.id)
                }
            }
        } catch (e: Exception) {
            // Polling failed - will retry
        }
    }

    private suspend fun sendLocationForRequest(istekId: Int, teknisyenId: Int) {
        val location = lastKnownLocation ?: return

        val request = LocationRequestDto.SendRequest(
            istek_id = istekId,
            teknisyen_id = teknisyenId,
            lat = location.latitude,
            lng = location.longitude,
            acc = location.accuracy,
            t = java.time.Instant.now().toString()
        )

        try {
            apiService.sendLocation(request)
        } catch (e: Exception) {
            // Failed to send location
        }
    }
}