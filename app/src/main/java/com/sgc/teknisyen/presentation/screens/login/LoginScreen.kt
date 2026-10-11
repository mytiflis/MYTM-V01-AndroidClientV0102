package com.sgc.teknisyen.presentation.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sgc.teknisyen.R
import com.sgc.teknisyen.domain.model.AuthResult
import com.sgc.teknisyen.SgcTeknisyenTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val viewModel: LoginViewModel = hiltViewModel()
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Auto-navigate on success
    androidx.compose.runtime.LaunchedEffect(authState) {
        if (authState is AuthResult.Authenticated) {
            onLoginSuccess()
        }
    }

    SgcTeknisyenTheme {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Card(
                    modifier = Modifier.size(120.dp),
                    elevation = 4.dp,
                    shape = androidx.compose.material3.MaterialTheme.shapes.large
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SGC",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(32.dp))

                Text(
                    text = stringResource(R.string.login_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = stringResource(R.string.login_subtitle),
                    fontSize = 16.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(32.dp))

                // Username field
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.hint_username)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))

                // Password field
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.hint_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions.Default
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))

                // Remember me checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it }
                    )
                    Text(stringResource(R.string.remember_me))
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))

                // Error message
                if (showError) {
                    Text(
                        text = errorMessage,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                }

                // Login button
                Button(
                    onClick = {
                        if (username.isNotBlank() && password.isNotBlank()) {
                            viewModel.login(username, password, rememberMe)
                        } else {
                            showError = true
                            errorMessage = stringResource(R.string.login_error_invalid)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = authState !is AuthResult.Loading
                ) {
                    if (authState is AuthResult.Loading) {
                        androidx.compose.material.ProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.btn_login),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}