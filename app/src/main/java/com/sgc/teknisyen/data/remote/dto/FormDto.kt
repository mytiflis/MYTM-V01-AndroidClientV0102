package com.sgc.teknisyen.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FormDto(
    // Nested classes - Şantiye Faaliyet Formu fields
) {
    @Serializable
    data class SubmitRequest(
        val is_emri_no: String,
        val teknisyen_id: Int,
        val tarih: String,                    // ISO 8601 date only
        val baslangic_saati: String,          // HH:mm
        val bitis_saati: String,              // HH:mm
        val konu: String,
        val yapilan_isler: String,
        val kullanilan_malzemeler: String,
        val arizalar: String?,
        val notlar: String?,
        val imza_base64: String?,             // signature as base64 PNG
        val foto_base64_list: List<String>?   // photos as base64
    )

    @Serializable
    data class SubmitResponse(
        val success: Boolean,
        val form_id: Int? = null,
        val message: String? = null
    )

    @Serializable
    data class CachedForm(
        val id: Int? = null,
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
        val foto_base64_list: String?,        // JSON array as string
        val created_at: Long,
        val synced: Boolean = false
    )
}