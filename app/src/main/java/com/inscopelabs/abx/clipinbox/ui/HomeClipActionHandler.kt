package com.inscopelabs.abx.clipinbox.ui

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.category.CategoryRepository
import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.domain.ClipRepository
import com.inscopelabs.abx.clipinbox.utils.ClipboardHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class HomeClipActionHandler(
    private val context: Context,
    private val scope: CoroutineScope,
    private val repository: ClipRepository,
    private val categoryRepository: CategoryRepository,
    private val childFragmentManager: FragmentManager,
    private val showMessage: (String) -> Unit,
    private val shareText: (String) -> Unit
) {

    fun onClipClick(clip: ClipEntity, callback: ClipActionBottomSheet.Callback) {
        Logger.d("HomeClipActionHandler", "onClipClick: clip id=${clip.id}")
        scope.launch {
            repository.markRead(clip)
        }
        val sheet = ClipActionBottomSheet.newInstance(clip, callback)
        sheet.show(childFragmentManager, ClipActionBottomSheet.TAG)
    }

    fun onCopyClick(clip: ClipEntity) {
        Logger.d("HomeClipActionHandler", "onCopyClick: clip id=${clip.id}")
        ClipboardHelper.copyToClipboard(context, clip.content)
        showMessage(context.getString(R.string.home_toast_copied_to_clipboard))
    }

    fun onCopyClip(clip: ClipEntity) = onCopyClick(clip)

    fun onPinClick(clip: ClipEntity) {
        Logger.d("HomeClipActionHandler", "onPinClick: clip id=${clip.id}")
        scope.launch {
            val updated = clip.copy(isPinned = !clip.isPinned)
            repository.updateClip(updated)
            Logger.i("HomeClipActionHandler", "Clip id=${clip.id} pin updated to ${updated.isPinned}")
            showMessage(context.getString(if (updated.isPinned) R.string.home_toast_clip_pinned else R.string.home_toast_clip_unpinned))
        }
    }

    fun onFavoriteClick(clip: ClipEntity) {
        Logger.d("HomeClipActionHandler", "onFavoriteClick: clip id=${clip.id}")
        scope.launch {
            val updated = clip.copy(isFavorite = !clip.isFavorite)
            repository.updateClip(updated)
            Logger.i("HomeClipActionHandler", "Clip id=${clip.id} favorite updated to ${updated.isFavorite}")
        }
    }

    fun onShareClick(clip: ClipEntity) {
        Logger.d("HomeClipActionHandler", "onShareClick: clip id=${clip.id}")
        shareText(clip.content)
    }

    fun onShareClip(clip: ClipEntity) = onShareClick(clip)

    fun onDeleteClick(clip: ClipEntity) {
        Logger.d("HomeClipActionHandler", "onDeleteClick: clip id=${clip.id}")
        scope.launch {
            repository.deleteClip(clip)
            Logger.i("HomeClipActionHandler", "Clip id=${clip.id} deleted")
            showMessage(context.getString(R.string.home_toast_clip_deleted))
        }
    }

    fun onSaveNewClip(text: String) {
        Logger.d("HomeClipActionHandler", "onSaveNewClip")
        scope.launch {
            val clipId = repository.saveClipText(text)
            if (clipId != null) {
                Logger.i("HomeClipActionHandler", "Saved new clip id=$clipId")
                showMessage(context.getString(R.string.home_toast_clip_saved))
                CategoryPickerDialogHelper.showIfEnabledAfterSave(
                    context,
                    scope,
                    categoryRepository,
                    repository,
                    clipId
                )
            } else {
                Logger.w("HomeClipActionHandler", "Clip already exists")
                showMessage(context.getString(R.string.home_toast_clip_exists))
            }
        }
    }

    fun onUpdateClip(clip: ClipEntity, newContent: String) {
        Logger.d("HomeClipActionHandler", "onUpdateClip: clip id=${clip.id}")
        scope.launch {
            val updated = clip.copy(
                content = newContent,
                charCount = newContent.length,
                wordCount = if (newContent.isBlank()) 0 else newContent.trim().split("\\s+".toRegex()).size
            )
            repository.updateClip(updated)
            Logger.i("HomeClipActionHandler", "Updated clip content id=${clip.id}")
            showMessage(context.getString(R.string.home_toast_clip_updated))
        }
    }

    fun onUpdateClipCategory(clip: ClipEntity, categoryId: Long, tags: String) {
        Logger.d("HomeClipActionHandler", "onUpdateClipCategory: clip id=${clip.id}, catId=$categoryId")
        scope.launch {
            repository.updateClip(clip.copy(categoryId = categoryId, tags = tags))
            Logger.i("HomeClipActionHandler", "Updated clip category id=${clip.id}")
            showMessage(context.getString(R.string.home_toast_clip_updated))
        }
    }

    fun onSplitClip(clip: ClipEntity, parts: List<String>, deleteOriginal: Boolean) {
        Logger.d("HomeClipActionHandler", "onSplitClip: clip id=${clip.id}, parts=${parts.size}, deleteOriginal=$deleteOriginal")
        scope.launch {
            for (part in parts) {
                val id = repository.saveClipText(part)
                if (id != null) {
                    repository.getClipById(id)?.let { newClip ->
                        repository.updateClip(newClip.copy(categoryId = clip.categoryId, tags = clip.tags))
                    }
                }
            }
            if (deleteOriginal) {
                repository.deleteClip(clip)
            }
            Logger.i("HomeClipActionHandler", "Split clip id=${clip.id} into ${parts.size} parts")
            showMessage(context.getString(R.string.home_toast_clip_split, parts.size))
        }
    }

    fun clearUnpinned() {
        scope.launch {
            repository.clearUnpinned()
            Logger.i("HomeClipActionHandler", "Cleared unpinned clips")
            showMessage(context.getString(R.string.home_toast_cleared_unpinned))
        }
    }
}
