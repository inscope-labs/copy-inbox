package com.inscopelabs.abx.clipinbox.domain.queue

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: QueueEntity): Long

    // upsert: if id == 0 it inserts; if id > 0 it replaces (REPLACE strategy covers both)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSync(entity: QueueEntity): Long

    @Query("SELECT * FROM queue WHERE state = 'PENDING' ORDER BY createdAt ASC LIMIT :limit")
    suspend fun nextPending(limit: Int): List<QueueEntity>

    @Query("UPDATE queue SET state = 'IN_FLIGHT', attempts = attempts + 1 WHERE id = :id")
    suspend fun markInFlight(id: Long)

    @Query("UPDATE queue SET state = 'SENT' WHERE id = :id")
    suspend fun markSent(id: Long)

    @Query("UPDATE queue SET state = 'FAILED', lastError = :error WHERE id = :id")
    suspend fun markFailed(id: Long, error: String?)

    @Query("SELECT * FROM queue ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<QueueEntity>>
}
