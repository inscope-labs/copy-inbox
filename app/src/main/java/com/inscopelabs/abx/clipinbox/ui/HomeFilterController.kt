package com.inscopelabs.abx.clipinbox.ui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.category.CategoryRepository
import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.data.local.TagEntity
import com.inscopelabs.abx.clipinbox.databinding.FragmentHomeBinding
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.tag.TagRepository
import kotlinx.coroutines.launch

class HomeFilterController(
    private val context: Context,
    private val viewLifecycleOwner: LifecycleOwner,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
    private val adapter: ClipListAdapter,
    private val onFilterChanged: () -> Unit,
    private val getLatestClips: () -> List<ClipEntity>,
    private val renderClips: (List<ClipEntity>) -> Unit
) {

    private companion object {
        private const val TAG = "HomeFilterController"
    }

    var searchQuery: String = ""
        private set

    var selectedCategory: String = "All"
        private set

    var selectedCategoryFilterId: Long? = null
        private set

    var selectedTagIds: Set<Long> = emptySet()
        private set

    var matchAllTags: Boolean = false
        private set

    private var availableTags: List<TagEntity> = emptyList()

    fun setupSearch(binding: FragmentHomeBinding) {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim().orEmpty()
                Logger.d(TAG, "Search query updated: $searchQuery")
                onFilterChanged()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun setupCategoryChips(binding: FragmentHomeBinding) {
        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull()
            selectedCategory = when (checkedId) {
                R.id.chip_favorites -> "Favorites"
                R.id.chip_text -> "Text"
                R.id.chip_link -> "Link"
                R.id.chip_code -> "Code"
                R.id.chip_note -> "Note"
                else -> "All"
            }
            Logger.d(TAG, "Selected category filter: $selectedCategory")
            onFilterChanged()
        }
    }

    fun setupTagFilter(binding: FragmentHomeBinding) {
        binding.btnTagFilter.setOnClickListener {
            if (availableTags.isEmpty()) {
                Logger.w(TAG, "Tag filter clicked with empty availableTags")
                return@setOnClickListener
            }
            val tagLabels = availableTags.map { it.label }.toTypedArray()
            val checkedItems = BooleanArray(availableTags.size) { i -> selectedTagIds.contains(availableTags[i].id) }
            val tempSelected = selectedTagIds.toMutableSet()

            AlertDialog.Builder(context)
                .setTitle(R.string.tag_filter_options)
                .setMultiChoiceItems(tagLabels, checkedItems) { _, which, isChecked ->
                    val tagId = availableTags[which].id
                    if (isChecked) tempSelected.add(tagId) else tempSelected.remove(tagId)
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    selectedTagIds = tempSelected.toSet()
                    Logger.i(TAG, "Tag selection updated: $selectedTagIds, matchAll=$matchAllTags")
                    updateTagFilterButtonText(binding)
                    onFilterChanged()
                }
                .setNeutralButton("Clear") { _, _ ->
                    selectedTagIds = emptySet()
                    Logger.i(TAG, "Cleared tag filter selection")
                    updateTagFilterButtonText(binding)
                    onFilterChanged()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    fun setupTagFilterOptions(binding: FragmentHomeBinding) {
        binding.btnTagFilterOptions.setOnClickListener {
            val checkBox = CheckBox(context).apply {
                setText(R.string.tag_filter_match_all)
                isChecked = matchAllTags
            }
            val container = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 24, 48, 24)
                addView(checkBox)
            }

            AlertDialog.Builder(context)
                .setTitle(R.string.tag_options_dialog_title)
                .setView(container)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val newMatchAll = checkBox.isChecked
                    if (matchAllTags != newMatchAll) {
                        matchAllTags = newMatchAll
                        Logger.i(TAG, "Updated matchAllTags mode to $matchAllTags")
                        onFilterChanged()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun updateTagFilterButtonText(binding: FragmentHomeBinding) {
        if (selectedTagIds.isEmpty()) {
            binding.btnTagFilter.text = context.getString(R.string.tag_all_filter)
        } else {
            val labels = availableTags.filter { selectedTagIds.contains(it.id) }.map { it.label }
            binding.btnTagFilter.text = if (labels.size <= 2) labels.joinToString(", ") else "${labels.size} Tags Selected"
        }
    }

    fun observeCategories(binding: FragmentHomeBinding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                categoryRepository.observeCategories().collect { categories ->
                    Logger.d(TAG, "Categories updated: count=${categories.size}")
                    adapter.updateCategoryColors(categories.associate { it.id to it.colorHex })

                    val spinnerItems = listOf(context.getString(R.string.category_all_filter)) + categories.map { it.name }
                    val spinnerAdapter = ArrayAdapter(
                        context,
                        android.R.layout.simple_spinner_item,
                        spinnerItems
                    ).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    binding.spinnerCategoryFilter.adapter = spinnerAdapter

                    val currentSelectedIdx = if (selectedCategoryFilterId == null) 0 else {
                        val idx = categories.indexOfFirst { it.id == selectedCategoryFilterId }
                        if (idx >= 0) idx + 1 else 0
                    }
                    binding.spinnerCategoryFilter.setSelection(currentSelectedIdx)

                    binding.spinnerCategoryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            selectedCategoryFilterId = if (position == 0) null else categories.getOrNull(position - 1)?.id
                            Logger.d(TAG, "Category filter selected id: $selectedCategoryFilterId")
                            renderClips(getLatestClips())
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                }
            }
        }
    }

    fun observeTags(binding: FragmentHomeBinding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tagRepository.observeTags().collect { tags ->
                    Logger.d(TAG, "Tags updated in filter controller: count=${tags.size}")
                    availableTags = tags
                    updateTagFilterButtonText(binding)
                }
            }
        }
    }
}

