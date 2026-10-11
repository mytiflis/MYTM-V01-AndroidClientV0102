package com.sgc.teknisyen.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class JobDto(
    // Nested classes
) {
    @Serializable
    data class ListItem(
        val no: String,
        val firma: String,
        val tarih: String,          // ISO 8601
        val oncelik: String,        // "ACIL" | "NORMAL" | "DUSUK"
        val is_durumu: String,      // "ATANDI" | "BASLADI" | "TAMAMLANDI"
        val toplam_nokta: Int? = 0
    )

    @Serializable
    data class Detail(
        val no: String,
        val firma: String,
        val adres: String,
        val telefon: String?,
        val tarih: String,
        val oncelik: String,
        val is_durumu: String,
        val urunler: List<Urun>,
        val latitude: Double?,
        val longitude: Double?
    )

    @Serializable
    data class Urun(
        val id: Int,
        val adi: String,
        val miktar: Int,
        val birim: String,
        val aciklama: String?
    )

    @Serializable
    data class CheckInRequest(
        val checkin_lat: Double,
        val checkin_lng: Double,
        val checkin_accuracy: Float,
        val checkin_t: String,
        val checkin_ua: String,
        val checkin_mock: Boolean,
        val checkin_source: String      // "gps" | "network" | "fused"
    )

    @Serializable
    data class CheckInResponse(
        val success: Boolean,
        val message: String? = null
    )

    @Serializable
    data class CheckOutRequest(
        val checkout_lat: Double,
        val checkout_lng: Double,
        val checkout_accuracy: Float,
        val checkout_t: String,
        val checkout_ua: String,
        val checkout_mock: Boolean,
        val checkout_source: String
    )

    @Serializable
    data class CheckOutResponse(
        val success: Boolean,
        val message: String? = null
    )

    @Serializable
    data class UpdateStatusRequest(
        val is_durumu: String          // "BASLADI" | "TAMAMLANDI"
    )

    @Serializable
    data class UpdateStatusResponse(
        val success: Boolean
    )

    @Serializable
    data class PhotoUploadResponse(
        val success: Boolean,
        val fileUrl: String? = null
    )

    companion object {
        fun createCheckInRequest(
            lat: Double,
            lng: Double,
            accuracy: Float,
            isMock: Boolean,
            source: String,
            userAgent: String
        ): CheckInRequest {
            return CheckInRequest(
                checkin_lat = lat,
                checkin_lng = lng,
                checkin_accuracy = accuracy,
                checkin_t = java.time.Instant.now().toString(),
                checkin_ua = userAgent,
                checkin_mock = isMock,
                checkin_source = source
            )
        }

        fun createCheckOutRequest(
            lat: Double,
            lng: Double,
            accuracy: Float,
            isMock: Boolean,
            source: String,
            userAgent: String
        ): CheckOutRequest {
            return CheckOutRequest(
                checkout_lat = lat,
                checkout_lng = lng,
                checkout_accuracy = accuracy,
                checkout_t = java.time.Instant.now().toString(),
                checkout_ua = userAgent,
                checkout_mock = isMock,
                checkout_source = source
            )
        }
    }
}