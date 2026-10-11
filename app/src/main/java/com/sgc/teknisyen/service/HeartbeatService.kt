package com.sgc.teknisyen.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.sgc.teknisyen.R
import com.sgc.teknisyen.data.remote.ApiService
import com.sgc.teknisyen.data.remote.dto.HeartbeatDto
import com.sgc.teknisyen.data.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HeartbeatService : LifecycleService() {

    private val NOTIFICATION_ID = 1002
    private val CHANNEL_ID = "sgc_heartbeat_channel"

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var apiService: ApiService

    private var notificationManager: NotificationManager? = null

    private var heartbeatJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            startForegroundService()
            startHeartbeatLoop()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        heartbeatJob?.cancel()
        coroutineScope.cancel()
        stopForeground(true)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SGC Heartbeat",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Periyodik canlılık sinyali gönderimi"
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
            .setContentText("Heartbeat aktif")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

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

        // Get last known location from LocationService (would need shared state)
        // For now, send without location
        val request = HeartbeatDto.Request.create(
            teknisyenId = technician.id,
            lat = null,
            lng = null,
            accuracy = null,
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
            // Retry on next cycle
        }
    }

    private fun getBatteryLevel(): Int = 100
    private fun isCharging(): Boolean = false

    private fun getNetworkType(): String {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "offline"
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return "unknown"
        return when {
            caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
            else -> "unknown"
        }
    }
}