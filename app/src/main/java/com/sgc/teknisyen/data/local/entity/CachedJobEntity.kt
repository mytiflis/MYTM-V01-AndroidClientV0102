package com.sgc.teknisyen.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "cached_jobs", indices = [androidx.room.Index(value = ["no"], unique = true)])
@Serializable
data class CachedJobEntity(
    @PrimaryKey val no: String,
    val firma: String,
    val adres: String,
    val telefon: String?,
    val tarih: String,
    val oncelik: String,
    val is_durumu: String,
    val urunler_json: String,          // JSON array of Urun
    val latitude: Double?,
    val longitude: Double?,
    val cachedAt: Long = System.currentTimeMillis()
)