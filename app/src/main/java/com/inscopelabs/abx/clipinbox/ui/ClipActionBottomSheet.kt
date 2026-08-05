package com.inscopelabs.abx.clipinbox.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.databinding.BottomSheetClipActionsBinding
import com.inscopelabs.abx.clipinbox.utils.TimeFormatter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ClipActionBottomSheet : BottomSheetDialogFragment() {

    interface Callback {
        fun onSaveNewClip(text: String)
        fun onUpdateClip(clip: ClipEntity, newContent: String)
        fun onShareClip(clip: ClipEntity)
        fun onCopyClip(clip: ClipEntity)
        fun onSelectForMultiSelect(clip: ClipEntity) {}
    }

    private var _binding: BottomSheetClipActionsBinding? = null
    private val binding get() = _binding!!

    private var clip: ClipEntity? = null
    private var callback: Callback? = null
    private var isEditing = false

    fun setClipAndCallback(clip: ClipEntity?, callback: Callback) {
        this.clip = clip
        this.callback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetClipActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentClip = clip
        if (currentClip == null) {
            setupAddMode()
        } else {
            setupViewMode(currentClip)
        }
    }

    private fun setupAddMode() {
        binding.tvSheetTitle.text = getString(R.string.sheet_title_add_new)
        binding.btnToggleEdit.isVisible = false
        binding.layoutMetadata.isVisible = false

        binding.etClipContent.isEnabled = true
        binding.etClipContent.setText("")
        binding.etClipContent.requestFocus()

        binding.btnSecondaryAction.text = getString(R.string.action_cancel)
        binding.btnSecondaryAction.setOnClickListener {
            dismiss()
        }

        binding.btnPrimaryAction.text = getString(R.string.action_save)
        binding.btnPrimaryAction.setOnClickListener {
            val text = binding.etClipContent.text?.toString()?.trim().orEmpty()
            if (text.isNotBlank()) {
                callback?.onSaveNewClip(text)
                dismiss()
            } else {
                Toast.makeText(context, getString(R.string.sheet_error_empty_content), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupViewMode(currentClip: ClipEntity) {
        binding.tvSheetTitle.text = getString(R.string.sheet_title_clip_details)
        binding.btnToggleEdit.isVisible = true
        binding.layoutMetadata.isVisible = true

        binding.tvSheetCategoryCounts.text = getString(
            R.string.sheet_category_meta_format,
            currentClip.detectedType,
            currentClip.charCount,
            currentClip.wordCount
        )
        binding.tvSheetTimestamp.text = TimeFormatter.formatDetailedTime(currentClip.timestamp)

        isEditing = false
        updateEditingState(currentClip)

        binding.btnToggleEdit.setOnClickListener {
            isEditing = !isEditing
            updateEditingState(currentClip)
        }

        binding.btnSecondaryAction.setOnClickListener {
            if (isEditing) {
                isEditing = false
                updateEditingState(currentClip)
            } else {
                callback?.onShareClip(currentClip)
                dismiss()
            }
        }

        binding.btnPrimaryAction.setOnClickListener {
            if (isEditing) {
                val newContent = binding.etClipContent.text?.toString()?.trim().orEmpty()
                if (newContent.isNotBlank()) {
                    callback?.onUpdateClip(currentClip, newContent)
                    dismiss()
                } else {
                    Toast.makeText(context, getString(R.string.sheet_error_empty_content_short), Toast.LENGTH_SHORT).show()
                }
            } else {
                callback?.onCopyClip(currentClip)
                dismiss()
            }
        }
    }

    private fun updateEditingState(currentClip: ClipEntity) {
        if (isEditing) {
            binding.tvSheetTitle.text = getString(R.string.sheet_title_edit_clip)
            binding.btnToggleEdit.text = getString(R.string.action_cancel)
            binding.etClipContent.isEnabled = true
            binding.etClipContent.setText(currentClip.content)

            binding.btnSecondaryAction.text = getString(R.string.action_cancel)
            binding.btnPrimaryAction.text = getString(R.string.action_save)
        } else {
            binding.tvSheetTitle.text = getString(R.string.sheet_title_clip_details)
            binding.btnToggleEdit.text = getString(R.string.action_edit)
            binding.etClipContent.isEnabled = false
            binding.etClipContent.setText(currentClip.content)

            binding.btnSecondaryAction.text = getString(R.string.action_share)
            binding.btnPrimaryAction.text = getString(R.string.action_copy)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ClipActionBottomSheet"

        fun newInstance(clip: ClipEntity?, callback: Callback): ClipActionBottomSheet {
            return ClipActionBottomSheet().apply {
                setClipAndCallback(clip, callback)
            }
        }
    }
}
