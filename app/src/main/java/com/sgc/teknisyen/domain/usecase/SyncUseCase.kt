package com.sgc.teknisyen.domain.usecase

import com.sgc.teknisyen.data.repository.SyncRepository
import com.sgc.teknisyen.data.remote.dto.SyncDto
import javax.inject.Inject

class SyncUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    operator fun invoke(): SyncDto.BatchResponse? {
        return syncRepository.performBatchSync()
    }

    fun enqueueOperation(op: SyncDto.Operation) {
        syncRepository.enqueueOperation(op)
    }

    fun getPendingCount(): Int {
        return syncRepository.getPendingOperations().size
    }
}