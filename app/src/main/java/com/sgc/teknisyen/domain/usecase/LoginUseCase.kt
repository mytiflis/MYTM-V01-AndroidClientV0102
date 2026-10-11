package com.sgc.teknisyen.domain.usecase

import com.sgc.teknisyen.data.repository.AuthRepository
import com.sgc.teknisyen.domain.model.AuthResult
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(username: String, password: String, rememberMe: Boolean): AuthResult {
        return authRepository.login(username, password, rememberMe)
    }
}