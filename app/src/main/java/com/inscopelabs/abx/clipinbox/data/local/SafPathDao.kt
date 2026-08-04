package com.inscopelabs.abx.clipinbox.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SafPathDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(path: SafPath): Long

    @Update
    suspend fun update(path: SafPath)

    @Delete
    suspend fun delete(path: SafPath)

    @Query("SELECT * FROM saf_paths ORDER BY lastUsedAt DESC")
    fun observeAll(): Flow<List<SafPath>>

    @Query("SELECT * FROM saf_paths ORDER BY lastUsedAt DESC LIMIT 1")
    suspend fun lastUsed(): SafPath?

    @Query("UPDATE saf_paths SET lastUsedAt = :ts, seqCounter = seqCounter + 1 WHERE id = :id")
    suspend fun recordUse(id: Long, ts: Long)
}
