package com.inscopelabs.abx.clipinbox.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags WHERE isDeleted = 0 ORDER BY isSystemReserved DESC, label ASC")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY isSystemReserved DESC, label ASC")
    fun observeAllIncludingDeleted(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity): Long

    @Query("UPDATE tags SET isDeleted = 1 WHERE id = :tagId")
    suspend fun softDelete(tagId: Long)

    @Query("SELECT * FROM tags WHERE id = :tagId LIMIT 1")
    suspend fun getById(tagId: Long): TagEntity?

    @Query(
        """
        SELECT t.* FROM tags t
        INNER JOIN clip_tag_cross_ref ref ON t.id = ref.tagId
        WHERE ref.clipId = :clipId
        ORDER BY t.label ASC
        """
    )
    fun observeTagsForClip(clipId: Long): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(ref: ClipTagCrossRef)

    @Query("DELETE FROM clip_tag_cross_ref WHERE clipId = :clipId AND tagId = :tagId")
    suspend fun deleteCrossRef(clipId: Long, tagId: Long)

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun countAll(): Int

    @Query(
        """
        SELECT DISTINCT c.* FROM clips c
        INNER JOIN clip_tag_cross_ref ref ON c.id = ref.clipId
        WHERE c.isArchived = 0 AND ref.tagId IN (:tagIds)
        ORDER BY c.isPinned DESC, c.timestamp DESC
        """
    )
    fun observeClipsForTagsAny(tagIds: List<Long>): Flow<List<ClipEntity>>

    @Query(
        """
        SELECT c.* FROM clips c
        INNER JOIN clip_tag_cross_ref ref ON c.id = ref.clipId
        WHERE c.isArchived = 0 AND ref.tagId IN (:tagIds)
        GROUP BY c.id
        HAVING COUNT(DISTINCT ref.tagId) = :tagCount
        ORDER BY c.isPinned DESC, c.timestamp DESC
        """
    )
    fun observeClipsForTagsAll(tagIds: List<Long>, tagCount: Int): Flow<List<ClipEntity>>
}
