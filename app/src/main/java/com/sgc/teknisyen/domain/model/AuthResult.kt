package com.sgc.teknisyen.domain.model

import com.sgc.teknisyen.data.remote.dto.AuthDto

sealed class AuthResult {
    data class Authenticated(
        val token: String,
        val user: AuthDto.UserInfo,
        val teknisyen: AuthDto.TechnicianInfo
    ) : AuthResult()

    data class Unauthenticated(val message: String? = null) : AuthResult()

    data class Loading() : AuthResult()

    data class Error(val message: String) : AuthResult()
}