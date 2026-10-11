package com.sgc.teknisyen.domain.model

import com.sgc.teknisyen.data.remote.dto.SyncDto
import kotlinx.serialization.Serializable

@Serializable
data class PendingOperation(
    val id: Int,
    val type: String,               // "checkin" | "checkout" | "durum" | "foto" | "form"
    val payload: String,            // JSON
    val createdAt: Long,
    val retryCount: Int,
    val lastAttemptAt: Long,
    val isSynced: Boolean
) {
    companion object {
        fun fromEntity(entity: com.sgc.teknisyen.data.local.entity.PendingOpEntity): PendingOperation {
            return PendingOperation(
                id = entity.id,
                type = entity.type,
                payload = entity.payload,
                createdAt = entity.createdAt,
                retryCount = entity.retryCount,
                lastAttemptAt = entity.lastAttemptAt,
                isSynced = entity.isSynced
            )
        }

        fun toOperation(op: PendingOperation): SyncDto.Operation {
            return when (op.type) {
                "checkin" -> kotlinx.serialization.json.Json.decodeFromString<SyncDto.Operation.CheckIn>(op.payload)
                "checkout" -> kotlinx.serialization.json.Json.decodeFromString<SyncDto.Operation.CheckOut>(op.payload)
                "durum" -> kotlinx.serialization.json.Json.decodeFromString<SyncDto.Operation.Status>(op.payload)
                "foto" -> kotlinx.serialization.json.Json.decodeFromString<SyncDto.Operation.Photo>(op.payload)
                "form" -> kotlinx.serialization.json.Json.decodeFromString<SyncDto.Operation.Form>(op.payload)
                else -> throw IllegalArgumentException("Unknown operation type: ${op.type}")
            }
        }
    }
}