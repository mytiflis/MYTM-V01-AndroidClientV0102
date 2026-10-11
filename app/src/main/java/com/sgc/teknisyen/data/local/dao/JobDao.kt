package com.sgc.teknisyen.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sgc.teknisyen.data.local.entity.CachedJobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jobs: List<CachedJobEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: CachedJobEntity)

    @Query("SELECT * FROM cached_jobs ORDER BY tarih DESC")
    suspend fun getAll(): List<CachedJobEntity>

    @Query("SELECT * FROM cached_jobs ORDER BY tarih DESC")
    fun observeAll(): Flow<List<CachedJobEntity>>

    @Query("SELECT * FROM cached_jobs WHERE no = :jobNo")
    suspend fun getByNo(jobNo: String): CachedJobEntity?

    @Query("SELECT * FROM cached_jobs WHERE no = :jobNo")
    fun observeByNo(jobNo: String): Flow<CachedJobEntity?>

    @Query("DELETE FROM cached_jobs")
    suspend fun clearAll(): Int

    @Query("DELETE FROM cached_jobs WHERE cachedAt < :beforeTimestamp")
    suspend fun deleteOld(beforeTimestamp: Long): Int
}