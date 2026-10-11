package com.sgc.teknisyen.di

import com.sgc.teknisyen.data.local.dao.FormDao
import com.sgc.teknisyen.data.local.dao.JobDao
import com.sgc.teknisyen.data.local.dao.PendingOpDao
import com.sgc.teknisyen.data.remote.ApiService
import com.sgc.teknisyen.data.repository.AuthRepository
import com.sgc.teknisyen.data.repository.AuthRepositoryImpl
import com.sgc.teknisyen.data.repository.JobRepository
import com.sgc.teknisyen.data.repository.JobRepositoryImpl
import com.sgc.teknisyen.data.repository.SyncRepository
import com.sgc.teknisyen.data.repository.SyncRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: ApiService,
        authInterceptor: com.sgc.teknisyen.data.remote.interceptor.AuthInterceptor
    ): AuthRepository {
        return AuthRepositoryImpl(apiService, authInterceptor)
    }

    @Provides
    @Singleton
    fun provideJobRepository(
        apiService: ApiService,
        jobDao: JobDao
    ): JobRepository {
        return JobRepositoryImpl(apiService, jobDao)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(
        apiService: ApiService,
        pendingOpDao: PendingOpDao,
        formDao: FormDao
    ): SyncRepository {
        return SyncRepositoryImpl(apiService, pendingOpDao, formDao)
    }
}