package com.sgc.teknisyen.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HeartbeatDto(
    // Nested classes
) {
    @Serializable
    data class Request(
        val teknisyen_id: Int,
        val t: String,                    // ISO 8601 UTC
        val lat: Double?,                 // nullable - GPS may be unavailable
        val lng: Double?,
        val acc: Float?,                  // accuracy in meters
        val battery: Int,                 // 0-100
        val charging: Boolean,
        val network: String,              // "wifi" | "4g" | "5g" | "offline"
        val device: DeviceInfo
    )

    @Serializable
    data class DeviceInfo(
        val model: String,
        val os: String,                   // "Android 14"
        val app_version: String
    )

    @Serializable
    data class Response(
        val success: Boolean,
        val serverTime: String? = null    // ISO 8601 UTC for time sync
    )

    companion object {
        fun create(
            teknisyenId: Int,
            lat: Double?,
            lng: Double?,
            accuracy: Float?,
            batteryLevel: Int,
            isCharging: Boolean,
            networkType: String,
            deviceModel: String,
            osVersion: String,
            appVersion: String
        ): Request {
            return Request(
                teknisyen_id = teknisyenId,
                t = java.time.Instant.now().toString(),
                lat = lat,
                lng = lng,
                acc = accuracy,
                battery = batteryLevel,
                charging = isCharging,
                network = networkType,
                device = DeviceInfo(
                    model = deviceModel,
                    os = "Android $osVersion",
                    app_version = appVersion
                )
            )
        }
    }
}