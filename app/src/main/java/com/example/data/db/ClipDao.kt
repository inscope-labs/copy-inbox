package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClipItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipDao {
    @Query("SELECT * FROM clip_items WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllActiveClips(): Flow<List<ClipItem>>

    @Query("SELECT * FROM clip_items WHERE isPinned = 1 AND isArchived = 0 ORDER BY updatedAt DESC")
    fun getPinnedClips(): Flow<List<ClipItem>>

    @Query("SELECT * FROM clip_items WHERE category = :category AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getClipsByCategory(category: String): Flow<List<ClipItem>>

    @Query("SELECT * FROM clip_items WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun searchClips(query: String): Flow<List<ClipItem>>

    @Query("SELECT * FROM clip_items WHERE id = :id")
    suspend fun getClipById(id: Long): ClipItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClip(clip: ClipItem): Long

    @Update
    suspend fun updateClip(clip: ClipItem)

    @Delete
    suspend fun deleteClip(clip: ClipItem)

    @Query("DELETE FROM clip_items WHERE id = :id")
    suspend fun deleteClipById(id: Long)

    @Query("UPDATE clip_items SET copyCount = copyCount + 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun incrementCopyCount(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE clip_items SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePinStatus(id: Long, isPinned: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM clip_items WHERE isArchived = 0")
    fun getTotalClipCount(): Flow<Int>

    @Query("SELECT SUM(copyCount) FROM clip_items")
    fun getTotalCopiesCount(): Flow<Int?>
}
