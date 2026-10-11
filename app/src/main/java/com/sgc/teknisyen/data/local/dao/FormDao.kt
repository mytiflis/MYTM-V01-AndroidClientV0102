package com.sgc.teknisyen.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sgc.teknisyen.data.local.entity.CachedFormEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FormDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(form: CachedFormEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(forms: List<CachedFormEntity>)

    @Update
    suspend fun update(form: CachedFormEntity): Int

    @Query("SELECT * FROM cached_forms WHERE is_emri_no = :jobNo ORDER BY created_at DESC")
    suspend fun getByJobNo(jobNo: String): List<CachedFormEntity>

    @Query("SELECT * FROM cached_forms WHERE synced = 0 ORDER BY created_at ASC")
    suspend fun getUnsynced(): List<CachedFormEntity>

    @Query("SELECT * FROM cached_forms WHERE synced = 0 ORDER BY created_at ASC")
    fun observeUnsynced(): Flow<List<CachedFormEntity>>

    @Query("SELECT COUNT(*) FROM cached_forms WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("DELETE FROM cached_forms WHERE id = :id")
    suspend fun deleteById(id: Int): Int
}