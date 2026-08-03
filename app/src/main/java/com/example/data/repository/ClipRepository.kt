package com.example.data.repository

import com.example.data.db.ClipDao
import com.example.data.model.ClipItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ClipRepository(private val clipDao: ClipDao) {

    val allActiveClips: Flow<List<ClipItem>> = clipDao.getAllActiveClips()
    val pinnedClips: Flow<List<ClipItem>> = clipDao.getPinnedClips()
    val totalClipCount: Flow<Int> = clipDao.getTotalClipCount()
    val totalCopiesCount: Flow<Int?> = clipDao.getTotalCopiesCount()

    fun getClipsByCategory(category: String): Flow<List<ClipItem>> {
        return if (category == "All") {
            clipDao.getAllActiveClips()
        } else if (category == "Pinned") {
            clipDao.getPinnedClips()
        } else {
            clipDao.getClipsByCategory(category)
        }
    }

    fun searchClips(query: String): Flow<List<ClipItem>> {
        return clipDao.searchClips(query)
    }

    suspend fun insert(clip: ClipItem): Long {
        return clipDao.insertClip(clip)
    }

    suspend fun update(clip: ClipItem) {
        clipDao.updateClip(clip)
    }

    suspend fun delete(clip: ClipItem) {
        clipDao.deleteClip(clip)
    }

    suspend fun incrementCopy(id: Long) {
        clipDao.incrementCopyCount(id)
    }

    suspend fun togglePin(id: Long, currentPinState: Boolean) {
        clipDao.updatePinStatus(id, !currentPinState)
    }

    suspend fun populateDefaultClipsIfEmpty() {
        val currentClips = clipDao.getAllActiveClips().first()
        if (currentClips.isEmpty()) {
            val defaults = listOf(
                ClipItem(
                    title = "Kotlin Flow Collect Signature",
                    content = "viewModel.uiState.collectAsStateWithLifecycle()",
                    category = "Code",
                    isPinned = true,
                    colorTagIndex = 2
                ),
                ClipItem(
                    title = "Android Dev Docs",
                    content = "https://developer.android.com/develop/ui/compose",
                    category = "Link",
                    isPinned = true,
                    colorTagIndex = 1
                ),
                ClipItem(
                    title = "Standup Update Template",
                    content = "Yesterday: Worked on Copy Inbox Room DB integration.\nToday: Implementing Jetpack Compose UI and quick clipboard sync.\nBlockers: None.",
                    category = "Work",
                    isPinned = false,
                    colorTagIndex = 3
                ),
                ClipItem(
                    title = "Wi-Fi Guest Password",
                    content = "CoffeeShopGuest2026!",
                    category = "Notes",
                    isPinned = false,
                    colorTagIndex = 0
                ),
                ClipItem(
                    title = "Design Token Hex Color",
                    content = "#4F46E5",
                    category = "Code",
                    isPinned = false,
                    colorTagIndex = 5
                )
            )
            defaults.forEach { clipDao.insertClip(it) }
        }
    }
}
