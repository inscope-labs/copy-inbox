package com.example.ui.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ClipItem
import com.example.data.repository.ClipRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ClipStats(
    val totalClips: Int = 0,
    val pinnedClips: Int = 0,
    val totalCopies: Int = 0
)

class ClipViewModel(private val repository: ClipRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _detectedClipboardText = MutableStateFlow<String?>(null)
    val detectedClipboardText: StateFlow<String?> = _detectedClipboardText.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.populateDefaultClipsIfEmpty()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val clips: StateFlow<List<ClipItem>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        Pair(query, category)
    }.flatMapLatest { (query, category) ->
        if (query.isNotBlank()) {
            repository.searchClips(query.trim())
        } else {
            repository.getClipsByCategory(category)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val stats: StateFlow<ClipStats> = combine(
        repository.allActiveClips,
        repository.totalCopiesCount
    ) { allClips, totalCopies ->
        ClipStats(
            totalClips = allClips.size,
            pinnedClips = allClips.count { it.isPinned },
            totalCopies = totalCopies ?: 0
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClipStats()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun saveClip(
        title: String,
        content: String,
        category: String,
        colorTagIndex: Int = 0,
        id: Long = 0
    ) {
        if (content.isBlank()) return
        val finalTitle = if (title.isBlank()) {
            content.take(30).replace("\n", " ").trim() + if (content.length > 30) "..." else ""
        } else {
            title.trim()
        }

        viewModelScope.launch {
            val clip = ClipItem(
                id = id,
                title = finalTitle,
                content = content.trim(),
                category = category,
                colorTagIndex = colorTagIndex,
                updatedAt = System.currentTimeMillis()
            )
            if (id == 0L) {
                repository.insert(clip)
                _snackbarMessage.value = "Snippet saved to inbox"
            } else {
                repository.update(clip)
                _snackbarMessage.value = "Snippet updated"
            }
        }
    }

    fun deleteClip(clip: ClipItem) {
        viewModelScope.launch {
            repository.delete(clip)
            _snackbarMessage.value = "Snippet deleted"
        }
    }

    fun togglePin(clip: ClipItem) {
        viewModelScope.launch {
            repository.togglePin(clip.id, clip.isPinned)
            val actionText = if (!clip.isPinned) "Pinned snippet" else "Unpinned snippet"
            _snackbarMessage.value = actionText
        }
    }

    fun copyClipToClipboard(context: Context, clip: ClipItem) {
        copyTextToClipboard(context, clip.content, clip.title)
        viewModelScope.launch {
            repository.incrementCopy(clip.id)
            _snackbarMessage.value = "Copied '${clip.title}' to clipboard!"
        }
    }

    fun copyTextToClipboard(context: Context, text: String, label: String = "Copy Inbox") {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(label, text)
        clipboardManager.setPrimaryClip(clipData)
    }

    fun checkSystemClipboard(context: Context) {
        try {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboardManager.hasPrimaryClip()) {
                val clipData = clipboardManager.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val text = clipData.getItemAt(0).text?.toString()
                    if (!text.isNullOrBlank() && text != _detectedClipboardText.value) {
                        // Check if text is not already identical to the last saved clip
                        val currentClips = clips.value
                        val isAlreadyTop = currentClips.firstOrNull()?.content == text
                        if (!isAlreadyTop) {
                            _detectedClipboardText.value = text
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore clipboard access security exceptions on some platforms
        }
    }

    fun quickSaveDetectedClipboard(category: String = "Notes") {
        val text = _detectedClipboardText.value ?: return
        saveClip(
            title = "",
            content = text,
            category = category,
            colorTagIndex = 0
        )
        _detectedClipboardText.value = null
    }

    fun dismissDetectedClipboard() {
        _detectedClipboardText.value = null
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    class Factory(private val repository: ClipRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ClipViewModel::class.java)) {
                return ClipViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
