package com.sgc.teknisyen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.sgc.teknisyen.di.hiltViewModel
import com.sgc.teknisyen.presentation.navigation.SgcNavHost
import com.sgc.teknisyen.presentation.screens.login.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by hiltViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SgcTeknisyenTheme {
                SgcNavHost(loginViewModel)
            }
        }
    }
}