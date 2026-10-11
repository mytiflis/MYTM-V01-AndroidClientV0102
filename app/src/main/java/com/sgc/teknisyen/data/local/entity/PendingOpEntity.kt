package com.sgc.teknisyen.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "pending_ops")
@Serializable
data class PendingOpEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,                              // "checkin" | "checkout" | "durum" | "foto" | "form"
    val payload: String,                           // JSON string of the operation
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val lastAttemptAt: Long = 0,
    val isSynced: Boolean = false
)