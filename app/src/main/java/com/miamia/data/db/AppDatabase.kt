package com.miamia.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase

@Dao
interface ScanSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: ScanSessionEntity)

    @Query("SELECT * FROM scan_sessions ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLatest(): ScanSessionEntity?
}

@Dao
interface ScanAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attempt: ScanAttemptEntity)
}

@Dao
interface ValidatedIngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(validated: ValidatedIngredientEntity)
}

@Database(
    entities = [ScanSessionEntity::class, ScanAttemptEntity::class, ValidatedIngredientEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanSessionDao(): ScanSessionDao
    abstract fun scanAttemptDao(): ScanAttemptDao
    abstract fun validatedIngredientDao(): ValidatedIngredientDao
}
