package com.sgc.teknisyen.data.repository

import com.sgc.teknisyen.data.remote.ApiService
import com.sgc.teknisyen.data.remote.dto.AuthDto
import com.sgc.teknisyen.data.remote.interceptor.AuthInterceptor
import com.sgc.teknisyen.domain.model.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authInterceptor: AuthInterceptor
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Loading())
    override val authState = _authState.asStateFlow()

    override suspend fun login(username: String, password: String, rememberMe: Boolean): AuthResult {
        _authState.value = AuthResult.Loading()
        return try {
            val response = apiService.login(AuthDto.LoginRequest(username, password, rememberMe))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                authInterceptor.setToken(body.token)
                val result = AuthResult.Authenticated(body.token, body.user, body.teknisyen)
                _authState.value = result
                result
            } else {
                val error = response.message() ?: "Giriş başarısız"
                val result = AuthResult.Error(error)
                _authState.value = result
                result
            }
        } catch (e: Exception) {
            val result = AuthResult.Error(e.message ?: "Bağlantı hatası")
            _authState.value = result
            result
        }
    }

    override suspend fun logout() {
        try {
            apiService.logout()
        } catch (e: Exception) {
            // Ignore logout errors
        }
        authInterceptor.clearToken()
        _authState.value = AuthResult.Unauthenticated()
    }

    override suspend fun getMe(): AuthResult {
        return try {
            val response = apiService.getMe()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val result = AuthResult.Authenticated(
                    authInterceptor.getToken(),
                    body.user,
                    body.teknisyen
                )
                _authState.value = result
                result
            } else {
                val result = AuthResult.Error("Oturum doğrulanamadı")
                _authState.value = result
                result
            }
        } catch (e: Exception) {
            val result = AuthResult.Error(e.message ?: "Bağlantı hatası")
            _authState.value = result
            result
        }
    }

    override fun observeAuthState() = authState

    override fun isLoggedIn(): Boolean = authInterceptor.getToken().isNotBlank()

    override fun getCurrentUser(): AuthDto.UserInfo? {
        val state = _authState.value
        return if (state is AuthResult.Authenticated) state.user else null
    }

    override fun getCurrentTechnician(): AuthDto.TechnicianInfo? {
        val state = _authState.value
        return if (state is AuthResult.Authenticated) state.teknisyen else null
    }
}