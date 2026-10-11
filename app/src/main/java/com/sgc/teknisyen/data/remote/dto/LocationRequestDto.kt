package com.sgc.teknisyen.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationRequestDto(
    // Nested classes
) {
    @Serializable
    data class PendingResponse(
        val istek_id: Int? = null
    )

    @Serializable
    data class SendRequest(
        val istek_id: Int,
        val teknisyen_id: Int,
        val lat: Double,
        val lng: Double,
        val acc: Float,
        val t: String
    )

    @Serializable
    data class SendResponse(
        val success: Boolean,
        val receivedAt: String? = null
    )

    companion object {
        fun createSendRequest(
            istekId: Int,
            teknisyenId: Int,
            lat: Double,
            lng: Double,
            accuracy: Float
        ): SendRequest {
            return SendRequest(
                istek_id = istekId,
                teknisyen_id = teknisyenId,
                lat = lat,
                lng = lng,
                acc = accuracy,
                t = java.time.Instant.now().toString()
            )
        }
    }
}