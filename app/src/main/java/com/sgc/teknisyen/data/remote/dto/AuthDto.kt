package com.sgc.teknisyen.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthDto(
    // Request/Response classes are nested below
) {
    @Serializable
    data class LoginRequest(
        val username: String,
        val password: String,
        val rememberMe: Boolean = false
    )

    @Serializable
    data class LoginResponse(
        val token: String,
        val user: UserInfo,
        val teknisyen: TechnicianInfo
    )

    @Serializable
    data class LogoutResponse(
        val success: Boolean
    )

    @Serializable
    data class MeResponse(
        val user: UserInfo,
        val teknisyen: TechnicianInfo
    )

    @Serializable
    data class UserInfo(
        val id: Int,
        val username: String,
        val email: String?,
        val fullName: String,
        val role: String
    )

    @Serializable
    data class TechnicianInfo(
        val id: Int,
        val userId: Int,
        val firstName: String,
        val lastName: String,
        val phone: String?,
        val isActive: Boolean
    )
}