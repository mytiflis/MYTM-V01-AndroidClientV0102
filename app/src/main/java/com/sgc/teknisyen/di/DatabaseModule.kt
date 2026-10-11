package com.sgc.teknisyen.di

import android.content.Context
import com.sgc.teknisyen.data.local.SgcDatabase
import com.sgc.teknisyen.data.local.dao.FormDao
import com.sgc.teknisyen.data.local.dao.JobDao
import com.sgc.teknisyen.data.local.dao.PendingOpDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@Suppress("UNUSED_PARAMETER") context: Context): SgcDatabase {
        return SgcDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun providePendingOpDao(database: SgcDatabase): PendingOpDao {
        return database.pendingOpDao()
    }

    @Provides
    @Singleton
    fun provideJobDao(database: SgcDatabase): JobDao {
        return database.jobDao()
    }

    @Provides
    @Singleton
    fun provideFormDao(database: SgcDatabase): FormDao {
        return database.formDao()
    }
}