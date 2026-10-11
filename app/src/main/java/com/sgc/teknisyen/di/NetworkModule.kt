package com.sgc.teknisyen.di

import android.content.Context
import com.sgc.teknisyen.data.remote.ApiClient
import com.sgc.teknisyen.data.remote.ApiService
import com.sgc.teknisyen.data.remote.interceptor.AuthInterceptor
import com.sgc.teknisyen.data.remote.interceptor.DeviceHeaderInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthInterceptor(@Suppress("UNUSED_PARAMETER") context: Context): AuthInterceptor {
        return AuthInterceptor(context)
    }

    @Provides
    @Singleton
    fun provideDeviceHeaderInterceptor(): DeviceHeaderInterceptor {
        return DeviceHeaderInterceptor()
    }

    @Provides
    @Singleton
    fun provideApiClient(
        context: Context,
        authInterceptor: AuthInterceptor,
        deviceHeaderInterceptor: DeviceHeaderInterceptor
    ): ApiClient {
        return ApiClient(context, authInterceptor, deviceHeaderInterceptor)
    }

    @Provides
    @Singleton
    fun provideApiService(apiClient: ApiClient): ApiService {
        return apiClient.service
    }
}