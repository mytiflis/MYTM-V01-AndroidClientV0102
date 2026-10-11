package com.sgc.teknisyen.data.repository

import com.sgc.teknisyen.data.remote.dto.AuthDto
import com.sgc.teknisyen.domain.model.AuthResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun login(username: String, password: String, rememberMe: Boolean): AuthResult

    suspend fun logout()

    suspend fun getMe(): AuthResult

    fun observeAuthState(): Flow<AuthResult>

    fun isLoggedIn(): Boolean

    fun getCurrentUser(): AuthDto.UserInfo?

    fun getCurrentTechnician(): AuthDto.TechnicianInfo?
}