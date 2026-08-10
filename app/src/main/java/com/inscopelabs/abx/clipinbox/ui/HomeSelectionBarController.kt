package com.inscopelabs.abx.clipinbox.ui

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.databinding.FragmentHomeBinding
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.domain.ClipRepository
import com.inscopelabs.abx.clipinbox.utility.ClipJoiner
import com.inscopelabs.abx.clipinbox.utils.ClipboardHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class HomeSelectionBarController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val repository: ClipRepository,
    private val adapter: ClipListAdapter,
    private val fragmentManager: FragmentManager,
    private val showMessage: (String) -> Unit
) {

    fun setupSelectionBar(binding: FragmentHomeBinding) {
        binding.btnCancelSelection.setOnClickListener {
            Logger.d("HomeSelectionBarController", "Cancel selection clicked")
            adapter.clearSelection()
        }

        binding.btnActionCopy.setOnClickListener {
            val selectedClips = adapter.getSelectedClips()
            if (selectedClips.isEmpty()) {
                Logger.w("HomeSelectionBarController", "Copy clicked with no selection")
                showMessage(context.getString(R.string.home_toast_no_clips_selected))
                return@setOnClickListener
            }
            val textToCopy = selectedClips.first().content
            ClipboardHelper.copyToClipboard(context, textToCopy)
            Logger.i("HomeSelectionBarController", "Copied first selected clip content")
            showMessage(context.getString(R.string.home_toast_copied_to_clipboard))
        }

        binding.btnActionJoin.setOnClickListener {
            val selectedClips = adapter.getSelectedClips()
            if (selectedClips.size < 2) {
                Logger.w("HomeSelectionBarController", "Join clicked with < 2 selection (${selectedClips.size})")
                showMessage(context.getString(R.string.home_toast_select_at_least_two))
                return@setOnClickListener
            }
            JoinClipsDialogHelper.show(context, selectedClips.size) { separator, deleteOriginals ->
                scope.launch {
                    val sortedClips = selectedClips.sortedBy { it.timestamp }
                    val joinedText = ClipJoiner.join(sortedClips.map { it.content }, separator)
                    val joinedId = repository.saveClipText(joinedText)
                    if (joinedId != null) {
                        repository.getClipById(joinedId)?.let { newClip ->
                            repository.updateClip(newClip.copy(categoryId = sortedClips.first().categoryId, tags = sortedClips.first().tags))
                        }
                    }
                    if (deleteOriginals) {
                        for (clip in selectedClips) {
                            repository.deleteClip(clip)
                        }
                    }
                    adapter.clearSelection()
                    Logger.i("HomeSelectionBarController", "Joined ${selectedClips.size} clips successfully")
                    showMessage(context.getString(R.string.home_toast_clips_joined))
                }
            }
        }

        binding.btnActionDelete.setOnClickListener {
            val selectedClips = adapter.getSelectedClips()
            if (selectedClips.isEmpty()) {
                Logger.w("HomeSelectionBarController", "Delete clicked with no selection")
                return@setOnClickListener
            }
            AlertDialog.Builder(context)
                .setTitle(R.string.storage_delete_confirm)
                .setMessage(context.getString(R.string.home_toast_deleted_clips_format, selectedClips.size))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    scope.launch {
                        for (clip in selectedClips) {
                            repository.deleteClip(clip)
                        }
                        adapter.clearSelection()
                        Logger.i("HomeSelectionBarController", "Deleted ${selectedClips.size} selected clips")
                        showMessage(context.getString(R.string.home_toast_deleted_clips_format, selectedClips.size))
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        binding.btnActionSave.setOnClickListener {
            val ids = adapter.getSelectedIds().toLongArray()
            if (ids.isEmpty()) {
                Logger.w("HomeSelectionBarController", "Save clicked with no selection")
                showMessage(context.getString(R.string.home_toast_no_clips_selected))
                return@setOnClickListener
            }
            Logger.d("HomeSelectionBarController", "Opening SaveToPathBottomSheet for ${ids.size} clips")
            SaveToPathBottomSheet.newInstance(ids)
                .show(fragmentManager, "save_to_path")
        }
    }

    fun onSelectionChanged(binding: FragmentHomeBinding, selectedCount: Int) {
        if (selectedCount > 0) {
            binding.llContextualBar.isVisible = true
            binding.tvSelectionCount.text = context.getString(R.string.home_selection_count_format, selectedCount)
            binding.fabAddClip.isVisible = false
        } else {
            binding.llContextualBar.isVisible = false
            binding.fabAddClip.isVisible = true
            adapter.clearSelection()
        }
    }
}
