package com.sgc.teknisyen.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Job(
    val no: String,
    val firma: String,
    val adres: String,
    val telefon: String?,
    val tarih: String,
    val oncelik: Priority,
    val isDurumu: Status,
    val urunler: List<Urun>,
    val latitude: Double?,
    val longitude: Double?
) {
    enum class Priority(val label: String, val color: Int) {
        ACIL("Acil", 0xFFF44336),
        NORMAL("Normal", 0xFF2196F3),
        DUSUK("Düşük", 0xFF4CAF50)
    }

    enum class Status(val label: String, val color: Int) {
        ATANDI("Atandı", 0xFFFF9800),
        BASLADI("Başladı", 0xFF2196F3),
        TAMAMLANDI("Tamamlandı", 0xFF4CAF50)
    }

    @Serializable
    data class Urun(
        val id: Int,
        val adi: String,
        val miktar: Int,
        val birim: String,
        val aciklama: String?
    )

    companion object {
        fun fromDto(dto: com.sgc.teknisyen.data.remote.dto.JobDto.Detail): Job {
            val priority = when (dto.oncelik.uppercase()) {
                "ACIL" -> Priority.ACIL
                "DUSUK", "DÜŞÜK" -> Priority.DUSUK
                else -> Priority.NORMAL
            }

            val status = when (dto.is_durumu.uppercase()) {
                "BASLADI", "BAŞLADI" -> Status.BASLADI
                "TAMAMLANDI" -> Status.TAMAMLANDI
                else -> Status.ATANDI
            }

            val urunler = dto.urunler.map { u ->
                Urun(u.id, u.adi, u.miktar, u.birim, u.aciklama)
            }

            return Job(
                no = dto.no,
                firma = dto.firma,
                adres = dto.adres,
                telefon = dto.telefon,
                tarih = dto.tarih,
                oncelik = priority,
                isDurumu = status,
                urunler = urunler,
                latitude = dto.latitude,
                longitude = dto.longitude
            )
        }

        fun fromListDto(dto: com.sgc.teknisyen.data.remote.dto.JobDto.ListItem): Job {
            val priority = when (dto.oncelik.uppercase()) {
                "ACIL" -> Priority.ACIL
                "DUSUK", "DÜŞÜK" -> Priority.DUSUK
                else -> Priority.NORMAL
            }

            val status = when (dto.is_durumu.uppercase()) {
                "BASLADI", "BAŞLADI" -> Status.BASLADI
                "TAMAMLANDI" -> Status.TAMAMLANDI
                else -> Status.ATANDI
            }

            return Job(
                no = dto.no,
                firma = dto.firma,
                adres = "",
                telefon = null,
                tarih = dto.tarih,
                oncelik = priority,
                isDurumu = status,
                urunler = emptyList(),
                latitude = null,
                longitude = null
            )
        }
    }
}