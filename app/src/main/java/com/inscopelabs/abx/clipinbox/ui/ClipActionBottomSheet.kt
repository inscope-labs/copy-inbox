package com.inscopelabs.abx.clipinbox.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.inscopelabs.abx.clipinbox.ClipInBoxApplication
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.databinding.BottomSheetClipActionsBinding
import com.inscopelabs.abx.clipinbox.utils.TimeFormatter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ClipActionBottomSheet : BottomSheetDialogFragment() {

    interface Callback {
        fun onSaveNewClip(text: String)
        fun onUpdateClip(clip: ClipEntity, newContent: String)
        fun onUpdateClipCategory(clip: ClipEntity, categoryId: Long, tags: String)
        fun onShareClip(clip: ClipEntity)
        fun onCopyClip(clip: ClipEntity)
        fun onSelectForMultiSelect(clip: ClipEntity) {}
        fun onSplitClip(clip: ClipEntity, parts: List<String>, deleteOriginal: Boolean) {}
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

        lifecycleScope.launch {
            try {
                val app = requireActivity().application as ClipInBoxApplication
                val categories = app.categoryRepository.observeCategories().first()
                val activeCategory = categories.firstOrNull { it.id == currentClip.categoryId }
                    ?: categories.firstOrNull { it.isDefault }
                    ?: categories.firstOrNull()

                val categoryName = activeCategory?.name ?: currentClip.detectedType
                val colorHex = activeCategory?.colorHex ?: "#9E9E9E"
                val dotColor = try { Color.parseColor(colorHex) } catch (_: Throwable) { Color.parseColor("#9E9E9E") }

                val dotDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(dotColor)
                }
                binding.viewSheetCategoryDot.background = dotDrawable
                binding.tvSheetCategoryName.text = categoryName

                if (currentClip.tags.isNotBlank()) {
                    binding.tvSheetTags.isVisible = true
                    binding.tvSheetTags.text = currentClip.tags
                } else {
                    binding.tvSheetTags.isVisible = false
                }

                binding.btnChangeCategory.setOnClickListener {
                    CategoryPickerDialogHelper.show(
                        context = requireContext(),
                        categories = categories,
                        currentCategoryId = currentClip.categoryId,
                        currentTags = currentClip.tags,
                        onConfirm = { catId, tags ->
                            callback?.onUpdateClipCategory(currentClip, catId, tags)
                            dismiss()
                        }
                    )
                }
            } catch (t: Throwable) {
                // If application context or categories fail to resolve
            }
        }

        isEditing = false
        updateEditingState(currentClip)

        binding.btnFindReplace.setOnClickListener {
            FindReplaceDialogHelper.show(
                requireContext(),
                binding.etClipContent.text?.toString().orEmpty()
            ) { newText ->
                binding.etClipContent.setText(newText)
            }
        }

        binding.btnSplitClip.setOnClickListener {
            SplitClipDialogHelper.show(
                requireContext(),
                currentClip.content
            ) { parts, deleteOriginal ->
                callback?.onSplitClip(currentClip, parts, deleteOriginal)
                dismiss()
            }
        }

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
        binding.btnFindReplace.isVisible = isEditing
        binding.btnSplitClip.isVisible = !isEditing
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
