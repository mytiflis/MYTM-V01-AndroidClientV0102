package com.sgc.teknisyen.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Form(
    val id: Int? = null,
    val isEmriNo: String,
    val teknisyenId: Int,
    val tarih: String,
    val baslangicSaati: String,
    val bitisSaati: String,
    val konu: String,
    val yapilanIsler: String,
    val kullanilanMalzemeler: String,
    val arizalar: String?,
    val notlar: String?,
    val imzaBase64: String?,
    val fotoBase64List: List<String>,
    val createdAt: Long,
    val synced: Boolean = false
) {
    companion object {
        fun fromDto(dto: com.sgc.teknisyen.data.remote.dto.FormDto.CachedForm): Form {
            val photos = dto.foto_base64_list_json?.let { 
                kotlinx.serialization.json.Json.decodeFromString<List<String>>(it) 
            } ?: emptyList()

            return Form(
                id = dto.id,
                isEmriNo = dto.is_emri_no,
                teknisyenId = dto.teknisyen_id,
                tarih = dto.tarih,
                baslangicSaati = dto.baslangic_saati,
                bitisSaati = dto.bitis_saati,
                konu = dto.konu,
                yapilanIsler = dto.yapilan_isler,
                kullanilanMalzemeler = dto.kullanilan_malzemeler,
                arizalar = dto.arizalar,
                notlar = dto.notlar,
                imzaBase64 = dto.imza_base64,
                fotoBase64List = photos,
                createdAt = dto.created_at,
                synced = dto.synced
            )
        }

        fun toDto(form: Form): com.sgc.teknisyen.data.remote.dto.FormDto.CachedForm {
            return com.sgc.teknisyen.data.remote.dto.FormDto.CachedForm(
                id = form.id,
                is_emri_no = form.isEmriNo,
                teknisyen_id = form.teknisyenId,
                tarih = form.tarih,
                baslangic_saati = form.baslangicSaati,
                bitis_saati = form.bitisSaati,
                konu = form.konu,
                yapilan_isler = form.yapilanIsler,
                kullanilan_malzemeler = form.kullanilanMalzemeler,
                arizalar = form.arizalar,
                notlar = form.notlar,
                imza_base64 = form.imzaBase64,
                foto_base64_list = kotlinx.serialization.json.Json.encodeToString(form.fotoBase64List),
                created_at = form.createdAt,
                synced = form.synced
            )
        }
    }
}