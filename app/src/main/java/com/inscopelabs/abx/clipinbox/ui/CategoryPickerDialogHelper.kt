package com.inscopelabs.abx.clipinbox.ui

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.category.CategoryPreferences
import com.inscopelabs.abx.clipinbox.category.CategoryRepository
import com.inscopelabs.abx.clipinbox.data.local.CategoryEntity
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.domain.ClipRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object CategoryPickerDialogHelper {

    private const val TAG = "CategoryPickerDialogHelper"

    fun show(
        context: Context,
        categories: List<CategoryEntity>,
        currentCategoryId: Long?,
        currentTags: String,
        onConfirm: (categoryId: Long, tags: String) -> Unit,
        onSkip: () -> Unit = {}
    ) {
        Logger.i(TAG, "show: categoriesCount=${categories.size}, currentCategoryId=$currentCategoryId, currentTags='$currentTags'")
        if (categories.isEmpty()) {
            Logger.w(TAG, "show: categories list is empty, skipping")
            onSkip()
            return
        }

        val names = categories.map { it.name }
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, names).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val spinner = Spinner(context).apply {
            this.adapter = adapter
        }

        val defaultIndex = categories.indexOfFirst { it.isDefault }.let { if (it >= 0) it else 0 }
        val initialIndex = if (currentCategoryId != null) {
            val idx = categories.indexOfFirst { it.id == currentCategoryId }
            if (idx >= 0) idx else defaultIndex
        } else {
            defaultIndex
        }
        spinner.setSelection(initialIndex)

        val etTags = EditText(context).apply {
            hint = context.getString(R.string.category_tags_hint)
            setText(currentTags)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
            addView(spinner)
            addView(etTags)
        }

        AlertDialog.Builder(context)
            .setTitle(R.string.category_picker_title)
            .setView(container)
            .setPositiveButton(R.string.action_save) { _, _ ->
                val selectedIndex = spinner.selectedItemPosition
                val selectedCatId = categories.getOrNull(selectedIndex)?.id ?: categories[0].id
                val trimmedTags = etTags.text.toString().trim()
                Logger.i(TAG, "onConfirm: selectedCatId=$selectedCatId, tags='$trimmedTags'")
                onConfirm(selectedCatId, trimmedTags)
            }
            .setNegativeButton(R.string.action_skip) { _, _ ->
                Logger.i(TAG, "onSkip clicked")
                onSkip()
            }
            .setOnCancelListener {
                Logger.i(TAG, "dialog cancelled")
                onSkip()
            }
            .show()
    }

    fun showIfEnabledAfterSave(
        context: Context,
        scope: CoroutineScope,
        categoryRepository: CategoryRepository,
        clipRepository: ClipRepository,
        clipId: Long,
        onFinished: () -> Unit = {}
    ) {
        if (!CategoryPreferences.isSaveDialogEnabled(context)) {
            Logger.d(TAG, "showIfEnabledAfterSave: dialog disabled in preferences")
            onFinished()
            return
        }

        Logger.i(TAG, "showIfEnabledAfterSave: checking clipId=$clipId")
        scope.launch(Dispatchers.IO) {
            val clip = clipRepository.getClipById(clipId)
            val categories = try {
                categoryRepository.observeCategories().first()
            } catch (t: Throwable) {
                Logger.e(TAG, "Error fetching categories for clipId=$clipId", t)
                emptyList()
            }

            withContext(Dispatchers.Main) {
                if (clip == null || categories.isEmpty()) {
                    Logger.w(TAG, "showIfEnabledAfterSave aborted: clipIsNull=${clip == null}, categoriesCount=${categories.size}")
                    onFinished()
                    return@withContext
                }

                show(
                    context = context,
                    categories = categories,
                    currentCategoryId = clip.categoryId,
                    currentTags = clip.tags,
                    onConfirm = { catId, tags ->
                        scope.launch(Dispatchers.IO) {
                            clipRepository.updateClip(clip.copy(categoryId = catId, tags = tags))
                            Logger.i(TAG, "Updated clipId=$clipId with categoryId=$catId, tags='$tags'")
                            withContext(Dispatchers.Main) {
                                onFinished()
                            }
                        }
                    },
                    onSkip = {
                        onFinished()
                    }
                )
            }
        }
    }
}
