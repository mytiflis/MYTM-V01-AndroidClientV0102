package com.sgc.teknisyen.data.remote.interceptor

import android.os.Build
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class DeviceHeaderInterceptor @javax.inject.Inject constructor() : Interceptor {

    private val userAgent = "SGC-Tech/%s (Android %s; %s)".format(
        BuildConfig.VERSION_NAME,
        Build.VERSION.RELEASE,
        Build.MODEL
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val requestBuilder = original.newBuilder()
            .header("x-device", Build.MODEL)
            .header("x-os", "Android ${Build.VERSION.RELEASE}")
            .header("x-app", BuildConfig.VERSION_NAME)
            .header("User-Agent", userAgent)

        return chain.proceed(requestBuilder.build())
    }
}