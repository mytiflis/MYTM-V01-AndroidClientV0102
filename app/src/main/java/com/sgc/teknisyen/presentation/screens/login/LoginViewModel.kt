package com.sgc.teknisyen.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sgc.teknisyen.domain.model.AuthResult
import com.sgc.teknisyen.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Loading())
    val authState = _authState.asStateFlow()

    fun login(username: String, password: String, rememberMe: Boolean) {
        _authState.value = AuthResult.Loading()
        viewModelScope.launch {
            val result = loginUseCase(username, password, rememberMe)
            _authState.value = result
        }
    }

    fun clearError() {
        val current = _authState.value
        if (current is AuthResult.Error) {
            _authState.value = AuthResult.Unauthenticated()
        }
    }
}