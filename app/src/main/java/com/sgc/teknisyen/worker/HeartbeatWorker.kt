package com.sgc.teknisyen.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sgc.teknisyen.data.remote.ApiService
import com.sgc.teknisyen.data.remote.dto.HeartbeatDto
import com.sgc.teknisyen.data.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class HeartbeatWorker @javax.inject.Inject constructor(
    @Suppress("UNUSED_PARAMETER") context: Context,
    @Suppress("UNUSED_PARAMETER") params: WorkerParameters
) : CoroutineWorker(context, params) {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var apiService: ApiService

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!authRepository.isLoggedIn()) {
            return Result.success()
        }

        val technician = authRepository.getCurrentTechnician() ?: return Result.success()

        // Note: Location would need to be fetched from FusedLocationProvider
        // For worker, we send heartbeat without location (LocationService handles location heartbeats)
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

        return try {
            val response = apiService.sendHeartbeat(request)
            if (response.isSuccessful) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun getBatteryLevel(): Int = 100
    private fun isCharging(): Boolean = false

    private fun getNetworkType(): String {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "offline"
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return "unknown"
        return when {
            caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
            else -> "unknown"
        }
    }

    companion object {
        const val WORK_NAME = "HeartbeatWorker"
        const val TAG = "heartbeat_worker"
    }
}