package com.sgc.teknisyen.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sgc.teknisyen.data.remote.interceptor.AuthInterceptor
import com.sgc.teknisyen.data.remote.interceptor.DeviceHeaderInterceptor
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Singleton
class ApiClient @Inject constructor(
    @ApplicationContext context: Context,
    authInterceptor: AuthInterceptor,
    deviceHeaderInterceptor: DeviceHeaderInterceptor
) {

    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(deviceHeaderInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .serializeNulls()
            .create()

        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val service: ApiService = retrofit.create(ApiService::class.java)
}