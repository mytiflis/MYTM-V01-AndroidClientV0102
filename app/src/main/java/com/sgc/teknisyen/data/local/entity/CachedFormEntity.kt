package com.sgc.teknisyen.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "cached_forms", indices = [androidx.room.Index(value = ["is_emri_no", "created_at"], unique = true)])
@Serializable
data class CachedFormEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val is_emri_no: String,
    val teknisyen_id: Int,
    val tarih: String,
    val baslangic_saati: String,
    val bitis_saati: String,
    val konu: String,
    val yapilan_isler: String,
    val kullanilan_malzemeler: String,
    val arizalar: String?,
    val notlar: String?,
    val imza_base64: String?,
    val foto_base64_list_json: String?,     // JSON array as string
    val created_at: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)