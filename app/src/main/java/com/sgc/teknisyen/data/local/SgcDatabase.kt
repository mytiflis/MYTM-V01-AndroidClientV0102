package com.sgc.teknisyen.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sgc.teknisyen.data.local.dao.FormDao
import com.sgc.teknisyen.data.local.dao.JobDao
import com.sgc.teknisyen.data.local.dao.PendingOpDao
import com.sgc.teknisyen.data.local.entity.CachedFormEntity
import com.sgc.teknisyen.data.local.entity.CachedJobEntity
import com.sgc.teknisyen.data.local.entity.PendingOpEntity

@Database(
    entities = [
        PendingOpEntity::class,
        CachedJobEntity::class,
        CachedFormEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SgcDatabase : RoomDatabase() {

    abstract fun pendingOpDao(): PendingOpDao
    abstract fun jobDao(): JobDao
    abstract fun formDao(): FormDao

    companion object {
        @Volatile private var INSTANCE: SgcDatabase? = null

        fun getInstance(context: Context): SgcDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SgcDatabase::class.java,
                    "sgc_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    // Room will auto-convert primitives, String, Date, etc.
    // Add custom converters here if needed for complex types
}