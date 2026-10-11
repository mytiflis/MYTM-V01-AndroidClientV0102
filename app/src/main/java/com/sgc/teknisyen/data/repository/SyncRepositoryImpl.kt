package com.sgc.teknisyen.data.repository

import com.sgc.teknisyen.data.local.dao.FormDao
import com.sgc.teknisyen.data.local.dao.PendingOpDao
import com.sgc.teknisyen.data.local.entity.CachedFormEntity
import com.sgc.teknisyen.data.local.entity.PendingOpEntity
import com.sgc.teknisyen.data.remote.ApiService
import com.sgc.teknisyen.data.remote.dto.FormDto
import com.sgc.teknisyen.data.remote.dto.SyncDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val pendingOpDao: PendingOpDao,
    private val formDao: FormDao
) : SyncRepository {

    private val _pendingCount = MutableStateFlow(0)

    override suspend fun enqueueOperation(op: SyncDto.Operation) {
        val payload = kotlinx.serialization.json.Json.encodeToString(op)
        val entity = PendingOpEntity(
            type = op.type,
            payload = payload
        )
        pendingOpDao.insert(entity)
        refreshCount()
    }

    override suspend fun enqueueOperations(ops: List<SyncDto.Operation>) {
        val entities = ops.map { op ->
            PendingOpEntity(
                type = op.type,
                payload = kotlinx.serialization.json.Json.encodeToString(op)
            )
        }
        pendingOpDao.insertAll(entities)
        refreshCount()
    }

    override suspend fun getPendingOperations(): List<SyncDto.Operation> {
        val entities = pendingOpDao.getUnsyncedOps()
        return entities.mapNotNull { entity ->
            try {
                kotlinx.serialization.json.Json.decodeFromString<SyncDto.Operation>(entity.payload)
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun observePendingCount(): Flow<Int> {
        return pendingOpDao.observeUnsyncedCount()
    }

    override suspend fun markSynced(operationIds: List<Int>) {
        operationIds.forEach { id ->
            pendingOpDao.getUnsyncedOps().firstOrNull { it.id == id }?.let { entity ->
                val updated = entity.copy(isSynced = true, lastAttemptAt = System.currentTimeMillis())
                pendingOpDao.update(updated)
            }
        }
        refreshCount()
    }

    override suspend fun markFailed(operationId: Int, error: String) {
        pendingOpDao.getUnsyncedOps().firstOrNull { it.id == operationId }?.let { entity ->
            val updated = entity.copy(
                retryCount = entity.retryCount + 1,
                lastAttemptAt = System.currentTimeMillis()
            )
            pendingOpDao.update(updated)
        }
        refreshCount()
    }

    override suspend fun incrementRetry(operationId: Int) {
        pendingOpDao.getUnsyncedOps().firstOrNull { it.id == operationId }?.let { entity ->
            val updated = entity.copy(
                retryCount = entity.retryCount + 1,
                lastAttemptAt = System.currentTimeMillis()
            )
            pendingOpDao.update(updated)
        }
    }

    override suspend fun cleanupOldSynced(beforeTimestamp: Long): Int {
        return pendingOpDao.deleteOldSynced(beforeTimestamp)
    }

    // Forms
    override suspend fun saveFormDraft(form: FormDto.CachedForm): Long {
        val entity = CachedFormEntity(
            id = form.id ?: 0,
            is_emri_no = form.is_emri_no,
            teknisyen_id = form.teknisyen_id,
            tarih = form.tarih,
            baslangic_saati = form.baslangic_saati,
            bitis_saati = form.bitis_saati,
            konu = form.konu,
            yapilan_isler = form.yapilan_isler,
            kullanilan_malzemeler = form.kullanilan_malzemeler,
            arizalar = form.arizalar,
            notlar = form.notlar,
            imza_base64 = form.imza_base64,
            foto_base64_list_json = form.foto_base64_list_json,
            created_at = form.created_at,
            synced = form.synced
        )
        return formDao.insert(entity)
    }

    override suspend fun getFormDrafts(jobNo: String): List<FormDto.CachedForm> {
        return formDao.getByJobNo(jobNo).map { entity ->
            FormDto.CachedForm(
                id = entity.id,
                is_emri_no = entity.is_emri_no,
                teknisyen_id = entity.teknisyen_id,
                tarih = entity.tarih,
                baslangic_saati = entity.baslangic_saati,
                bitis_saati = entity.bitis_saati,
                konu = entity.konu,
                yapilan_isler = entity.yapilan_isler,
                kullanilan_malzemeler = entity.kullanilan_malzemeler,
                arizalar = entity.arizalar,
                notlar = entity.notlar,
                imza_base64 = entity.imza_base64,
                foto_base64_list_json = entity.foto_base64_list_json,
                created_at = entity.created_at,
                synced = entity.synced
            )
        }
    }

    override suspend fun getUnsyncedForms(): List<FormDto.CachedForm> {
        return formDao.getUnsynced().map { entity ->
            FormDto.CachedForm(
                id = entity.id,
                is_emri_no = entity.is_emri_no,
                teknisyen_id = entity.teknisyen_id,
                tarih = entity.tarih,
                baslangic_saati = entity.baslangic_saati,
                bitis_saati = entity.bitis_saati,
                konu = entity.konu,
                yapilan_isler = entity.yapilan_isler,
                kullanilan_malzemeler = entity.kullanilan_malzemeler,
                arizalar = entity.arizalar,
                notlar = entity.notlar,
                imza_base64 = entity.imza_base64,
                foto_base64_list_json = entity.foto_base64_list_json,
                created_at = entity.created_at,
                synced = entity.synced
            )
        }
    }

    override suspend fun markFormSynced(formId: Int) {
        formDao.getByJobNo("") // Not directly available, would need a query by ID
        // TODO: Implement proper query by ID
    }

    // Sync execution
    override suspend fun performBatchSync(): SyncDto.BatchResponse? {
        val operations = getPendingOperations()
        if (operations.isEmpty()) return null

        val request = SyncDto.BatchRequest(islemler = operations)
        return try {
            val response = apiService.syncBatch(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                // Mark successful operations as synced
                body.results.filter { it.success }.forEach { result ->
                    markSynced(listOf(result.index))
                }
                // Mark failed for retry
                body.results.filter { !it.success }.forEach { result ->
                    markFailed(result.index, result.message ?: "Unknown error")
                }
                refreshCount()
                body
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun isOnline(): Boolean {
        // Simple connectivity check
        return try {
            val response = apiService.getNotifications()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    private fun refreshCount() {
        pendingOpDao.getUnsyncedCount().let { count ->
            _pendingCount.value = count
        }
    }
}