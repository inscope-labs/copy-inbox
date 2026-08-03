package com.inscopelabs.abx.clipinbox.domain

import com.inscopelabs.abx.clipinbox.data.local.ClipDao
import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.utils.ClipboardHelper
import com.inscopelabs.abx.clipinbox.utils.HashGenerator
import kotlinx.coroutines.flow.Flow

class ClipRepositoryImpl(private val clipDao: ClipDao) : ClipRepository {

    override fun getAllClips(): Flow<List<ClipEntity>> = clipDao.getAllClips()

    override fun searchClips(query: String): Flow<List<ClipEntity>> = clipDao.searchClips(query)

    override fun getClipsByCategory(category: String): Flow<List<ClipEntity>> = clipDao.getClipsByCategory(category)

    override fun getFavoriteClips(): Flow<List<ClipEntity>> = clipDao.getFavoriteClips()

    override suspend fun saveClipText(text: String, category: String?): Boolean {
        if (text.isBlank()) return false
        val trimmedText = text.trim()
        val hash = HashGenerator.sha256(trimmedText)

        val existing = clipDao.getClipByHash(hash)
        if (existing != null) {
            val updated = existing.copy(
                timestamp = System.currentTimeMillis()
            )
            clipDao.updateClip(updated)
            return true
        }

        val detectedCategory = category ?: ClipboardHelper.detectCategory(trimmedText)
        val newClip = ClipEntity(
            content = trimmedText,
            contentHash = hash,
            category = detectedCategory,
            timestamp = System.currentTimeMillis()
        )
        clipDao.insertClip(newClip)
        return true
    }

    override suspend fun updateClip(clip: ClipEntity) {
        clipDao.updateClip(clip)
    }

    override suspend fun deleteClip(clip: ClipEntity) {
        clipDao.deleteClip(clip)
    }

    override suspend fun clearAll() {
        clipDao.clearAll()
    }

    override suspend fun clearUnpinned() {
        clipDao.clearUnpinned()
    }
}
