package com.sgc.teknisyen.data.repository

import com.sgc.teknisyen.data.remote.dto.FormDto
import com.sgc.teknisyen.data.remote.dto.SyncDto
import kotlinx.coroutines.flow.Flow

interface SyncRepository {

    // Pending operations
    suspend fun enqueueOperation(op: SyncDto.Operation)

    suspend fun enqueueOperations(ops: List<SyncDto.Operation>)

    suspend fun getPendingOperations(): List<SyncDto.Operation>

    fun observePendingCount(): Flow<Int>

    suspend fun markSynced(operationIds: List<Int>)

    suspend fun markFailed(operationId: Int, error: String)

    suspend fun incrementRetry(operationId: Int)

    suspend fun cleanupOldSynced(beforeTimestamp: Long): Int

    // Forms
    suspend fun saveFormDraft(form: FormDto.CachedForm): Long

    suspend fun getFormDrafts(jobNo: String): List<FormDto.CachedForm>

    suspend fun getUnsyncedForms(): List<FormDto.CachedForm>

    suspend fun markFormSynced(formId: Int)

    // Sync execution
    suspend fun performBatchSync(): SyncDto.BatchResponse?

    suspend fun isOnline(): Boolean
}