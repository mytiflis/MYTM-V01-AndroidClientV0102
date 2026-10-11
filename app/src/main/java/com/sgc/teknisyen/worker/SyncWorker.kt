package com.sgc.teknisyen.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sgc.teknisyen.data.remote.ApiService
import com.sgc.teknisyen.data.remote.dto.SyncDto
import com.sgc.teknisyen.data.repository.AuthRepository
import com.sgc.teknisyen.data.repository.SyncRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SyncWorker @javax.inject.Inject constructor(
    @Suppress("UNUSED_PARAMETER") context: Context,
    @Suppress("UNUSED_PARAMETER") params: WorkerParameters
) : CoroutineWorker(context, params) {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var syncRepository: SyncRepository

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Check if user is logged in
        if (!authRepository.isLoggedIn()) {
            return Result.success()
        }

        // Check connectivity
        val isOnline = syncRepository.isOnline()
        if (!isOnline) {
            return Result.retry()
        }

        // Perform batch sync
        val response = syncRepository.performBatchSync()

        return if (response != null && response.success) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "SyncWorker"
        const val TAG = "sync_worker"
    }
}