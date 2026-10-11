package com.sgc.teknisyen.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.sgc.teknisyen.data.remote.dto.SyncDto
import java.util.concurrent.TimeUnit

object WorkManagerHelper {

    private const val SYNC_INTERVAL_MINUTES = 15
    private const val HEARTBEAT_INTERVAL_MINUTES = 10 // fallback only

    fun scheduleSyncWork(context: Context) {
        val syncWork = PeriodicWorkRequest.Builder(SyncWorker::class.java, SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES)
            .addTag(SyncWorker.TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWork
        )
    }

    fun scheduleHeartbeatWork(context: Context) {
        val heartbeatWork = PeriodicWorkRequest.Builder(HeartbeatWorker::class.java, HEARTBEAT_INTERVAL_MINUTES, TimeUnit.MINUTES)
            .addTag(HeartbeatWorker.TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            HeartbeatWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            heartbeatWork
        )
    }

    fun cancelAllWork(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }

    fun triggerImmediateSync(context: Context) {
        val syncWork = androidx.work.OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .addTag("manual_sync")
            .build()
        WorkManager.getInstance(context).enqueue(syncWork)
    }
}