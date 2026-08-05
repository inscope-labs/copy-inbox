package com.inscopelabs.abx.clipinbox.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipDao {
    @Query("SELECT * FROM clips WHERE isArchived = 0 ORDER BY isPinned DESC, timestamp DESC")
    fun getAllClips(): Flow<List<ClipEntity>>

    @Query("SELECT * FROM clips WHERE isArchived = 0 AND content LIKE '%' || :query || '%' ORDER BY isPinned DESC, timestamp DESC")
    fun searchClips(query: String): Flow<List<ClipEntity>>

    @Query("SELECT * FROM clips WHERE isArchived = 0 AND detectedType = :detectedType ORDER BY isPinned DESC, timestamp DESC")
    fun getClipsByDetectedType(detectedType: String): Flow<List<ClipEntity>>

    @Query("SELECT * FROM clips WHERE isArchived = 0 AND categoryId = :categoryId ORDER BY isPinned DESC, timestamp DESC")
    fun getClipsByCategoryId(categoryId: Long): Flow<List<ClipEntity>>

    @Query("UPDATE clips SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignCategory(oldCategoryId: Long, newCategoryId: Long)

    @Query("SELECT * FROM clips WHERE isArchived = 0 AND isFavorite = 1 ORDER BY isPinned DESC, timestamp DESC")
    fun getFavoriteClips(): Flow<List<ClipEntity>>

    @Query("SELECT * FROM clips WHERE isArchived = 0 AND isPinned = 1 ORDER BY timestamp DESC")
    fun getPinnedClips(): Flow<List<ClipEntity>>

    @Query("SELECT * FROM clips WHERE contentHash = :hash LIMIT 1")
    suspend fun getClipByHash(hash: String): ClipEntity?

    @Query("SELECT * FROM clips WHERE id = :id LIMIT 1")
    suspend fun getClipById(id: Long): ClipEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClip(clip: ClipEntity): Long

    @Update
    suspend fun updateClip(clip: ClipEntity)

    @Delete
    suspend fun deleteClip(clip: ClipEntity)

    @Query("DELETE FROM clips")
    suspend fun clearAll()

    @Query("DELETE FROM clips WHERE isPinned = 0")
    suspend fun clearUnpinned()
}
