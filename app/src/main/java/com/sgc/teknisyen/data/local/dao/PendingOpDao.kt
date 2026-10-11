package com.sgc.teknisyen.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sgc.teknisyen.data.local.entity.PendingOpEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOpDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(op: PendingOpEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(ops: List<PendingOpEntity>)

    @Update
    suspend fun update(op: PendingOpEntity): Int

    @Update
    suspend fun updateAll(ops: List<PendingOpEntity>)

    @Query("SELECT * FROM pending_ops WHERE isSynced = 0 ORDER BY createdAt ASC LIMIT 100")
    suspend fun getUnsyncedOps(): List<PendingOpEntity>

    @Query("SELECT * FROM pending_ops WHERE isSynced = 0 ORDER BY createdAt ASC")
    fun observeUnsyncedOps(): Flow<List<PendingOpEntity>>

    @Query("SELECT COUNT(*) FROM pending_ops WHERE isSynced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("SELECT COUNT(*) FROM pending_ops WHERE isSynced = 0")
    fun observeUnsyncedCount(): Flow<Int>

    @Query("DELETE FROM pending_ops WHERE isSynced = 1 AND lastAttemptAt < :beforeTimestamp")
    suspend fun deleteOldSynced(beforeTimestamp: Long): Int

    @Query("DELETE FROM pending_ops WHERE id = :id")
    suspend fun deleteById(id: Int): Int
}