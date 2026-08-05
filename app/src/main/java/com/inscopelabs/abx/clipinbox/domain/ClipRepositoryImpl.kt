package com.inscopelabs.abx.clipinbox.domain

import com.inscopelabs.abx.clipinbox.data.local.ClipDao
import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.utils.ClipboardHelper
import com.inscopelabs.abx.clipinbox.utils.HashGenerator
import kotlinx.coroutines.flow.Flow

import com.inscopelabs.abx.clipinbox.category.CategoryRepository

class ClipRepositoryImpl(
    private val clipDao: ClipDao,
    private val categoryRepository: CategoryRepository
) : ClipRepository {

    override fun getAllClips(): Flow<List<ClipEntity>> = clipDao.getAllClips()

    override fun getInboxClips(): Flow<List<ClipEntity>> = clipDao.getAllClips()

    override fun searchClips(query: String): Flow<List<ClipEntity>> = clipDao.searchClips(query)

    override fun getClipsByDetectedType(detectedType: String): Flow<List<ClipEntity>> = clipDao.getClipsByDetectedType(detectedType)

    override fun getFavoriteClips(): Flow<List<ClipEntity>> = clipDao.getFavoriteClips()

    override suspend fun getClipById(id: Long): ClipEntity? {
        Logger.d("ClipRepositoryImpl", "getClipById id: $id")
        return clipDao.getClipById(id)
    }

    override suspend fun saveClipText(text: String, category: String?): Long? {
        if (text.isBlank()) {
            Logger.d("ClipRepositoryImpl", "saveClipText ignored: text is blank")
            return null
        }
        val trimmedText = text.trim()
        val hash = HashGenerator.sha256(trimmedText)

        val existing = clipDao.getClipByHash(hash)
        if (existing != null) {
            Logger.i("ClipRepositoryImpl", "saveClipText updating existing clip hash: $hash")
            val updated = existing.copy(
                timestamp = System.currentTimeMillis(),
                isRead = false,
                isArchived = false
            )
            clipDao.updateClip(updated)
            return existing.id
        }

        val defaultCategoryId = categoryRepository.getDefaultCategory().id
        val detectedType = category ?: ClipboardHelper.detectType(trimmedText)
        val newClip = ClipEntity(
            content = trimmedText,
            contentHash = hash,
            detectedType = detectedType,
            categoryId = defaultCategoryId,
            timestamp = System.currentTimeMillis(),
            isArchived = false,
            isRead = false
        )
        val newId = clipDao.insertClip(newClip)
        Logger.i("ClipRepositoryImpl", "saveClipText inserted new clip id: $newId, categoryId: $defaultCategoryId, detectedType: $detectedType")
        return newId
    }

    override suspend fun updateClip(clip: ClipEntity) {
        Logger.d("ClipRepositoryImpl", "updateClip id: ${clip.id}")
        clipDao.updateClip(clip)
    }

    override suspend fun archiveClip(clip: ClipEntity) {
        Logger.i("ClipRepositoryImpl", "archiveClip id: ${clip.id}")
        clipDao.updateClip(clip.copy(isArchived = true))
    }

    override suspend fun markRead(clip: ClipEntity) {
        if (!clip.isRead) {
            Logger.d("ClipRepositoryImpl", "markRead id: ${clip.id}")
            clipDao.updateClip(clip.copy(isRead = true))
        }
    }

    override suspend fun deleteClip(clip: ClipEntity) {
        Logger.i("ClipRepositoryImpl", "deleteClip id: ${clip.id}")
        clipDao.deleteClip(clip)
    }

    override suspend fun clearAll() {
        Logger.w("ClipRepositoryImpl", "clearAll triggered")
        clipDao.clearAll()
    }

    override suspend fun clearUnpinned() {
        Logger.i("ClipRepositoryImpl", "clearUnpinned triggered")
        clipDao.clearUnpinned()
    }
}
