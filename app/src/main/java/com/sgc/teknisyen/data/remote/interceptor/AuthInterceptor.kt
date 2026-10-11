package com.sgc.teknisyen.data.remote.interceptor

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class AuthInterceptor @javax.inject.Inject constructor(
    @Suppress("UNUSED_PARAMETER") context: Context
) : Interceptor {

    private val prefs by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "sgc_secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = prefs.getString("auth_token", "") ?: ""
        val original = chain.request()

        val requestBuilder = original.newBuilder()
        if (token.isNotBlank()) {
            requestBuilder.header("x-token", token)
        }

        return chain.proceed(requestBuilder.build())
    }

    fun clearToken() {
        prefs.edit().remove("auth_token").apply()
    }

    fun getToken(): String = prefs.getString("auth_token", "") ?: ""

    fun setToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }
}