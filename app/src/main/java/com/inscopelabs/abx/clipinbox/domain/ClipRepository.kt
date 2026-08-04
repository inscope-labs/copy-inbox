package com.inscopelabs.abx.clipinbox.domain

import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import kotlinx.coroutines.flow.Flow

interface ClipRepository {
    fun getAllClips(): Flow<List<ClipEntity>>
    fun getInboxClips(): Flow<List<ClipEntity>>
    fun searchClips(query: String): Flow<List<ClipEntity>>
    fun getClipsByCategory(category: String): Flow<List<ClipEntity>>
    fun getFavoriteClips(): Flow<List<ClipEntity>>
    suspend fun getClipById(id: Long): ClipEntity?
    suspend fun saveClipText(text: String, category: String? = null): Boolean
    suspend fun updateClip(clip: ClipEntity)
    suspend fun archiveClip(clip: ClipEntity)
    suspend fun markRead(clip: ClipEntity)
    suspend fun deleteClip(clip: ClipEntity)
    suspend fun clearAll()
    suspend fun clearUnpinned()
}
