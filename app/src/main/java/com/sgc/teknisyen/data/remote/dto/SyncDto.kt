package com.sgc.teknisyen.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SyncDto(
    // Nested classes
) {
    @Serializable
    sealed class Operation {
        @Serializable
        data class CheckIn(
            val type: String = "checkin",
            val is_emri_no: String,
            val lat: Double,
            val lng: Double,
            val accuracy: Float,
            val timestamp: String,
            val userAgent: String,
            val isMock: Boolean,
            val source: String
        ) : Operation()

        @Serializable
        data class CheckOut(
            val type: String = "checkout",
            val is_emri_no: String,
            val lat: Double,
            val lng: Double,
            val accuracy: Float,
            val timestamp: String,
            val userAgent: String,
            val isMock: Boolean,
            val source: String
        ) : Operation()

        @Serializable
        data class Status(
            val type: String = "durum",
            val is_emri_no: String,
            val is_durumu: String
        ) : Operation()

        @Serializable
        data class Photo(
            val type: String = "foto",
            val is_emri_no: String,
            val filePath: String,
            val description: String
        ) : Operation()

        @Serializable
        data class Form(
            val type: String = "form",
            val formData: FormDto.CachedForm
        ) : Operation()
    }

    @Serializable
    data class BatchRequest(
        val islemler: List<Operation>
    )

    @Serializable
    data class BatchResponse(
        val success: Boolean,
        val results: List<OperationResult>,
        val serverTime: String? = null
    )

    @Serializable
    data class OperationResult(
        val index: Int,
        val success: Boolean,
        val message: String? = null
    )

    @Serializable
    data class NotificationsResponse(
        val liste: List<NotificationItem>
    )

    @Serializable
    data class NotificationItem(
        val id: Int,
        val tip: String,
        val baslik: String,
        val mesaj: String,
        val t: String
    )
}