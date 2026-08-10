package com.inscopelabs.abx.clipinbox.tag

import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.data.local.TagEntity
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun observeTags(): Flow<List<TagEntity>>
    suspend fun createTag(label: String, colorHex: String): Long
    suspend fun deleteTag(tagId: Long)
    suspend fun addTagToClip(clipId: Long, tagId: Long)
    suspend fun removeTagFromClip(clipId: Long, tagId: Long)
    fun observeTagsForClip(clipId: Long): Flow<List<TagEntity>>
    fun observeClipsForTags(tagIds: Set<Long>, matchAll: Boolean = false): Flow<List<ClipEntity>>
    suspend fun ensureSystemTagsSeeded()
}
