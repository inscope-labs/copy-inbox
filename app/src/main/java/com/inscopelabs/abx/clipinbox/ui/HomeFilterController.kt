package com.inscopelabs.abx.clipinbox.ui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.category.CategoryRepository
import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.databinding.FragmentHomeBinding
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import kotlinx.coroutines.launch

class HomeFilterController(
    private val context: Context,
    private val viewLifecycleOwner: LifecycleOwner,
    private val categoryRepository: CategoryRepository,
    private val adapter: ClipListAdapter,
    private val onFilterChanged: () -> Unit,
    private val getLatestClips: () -> List<ClipEntity>,
    private val renderClips: (List<ClipEntity>) -> Unit
) {

    var searchQuery: String = ""
        private set

    var selectedCategory: String = "All"
        private set

    var selectedCategoryFilterId: Long? = null
        private set

    fun setupSearch(binding: FragmentHomeBinding) {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim().orEmpty()
                Logger.d("HomeFilterController", "Search query updated: $searchQuery")
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
            Logger.d("HomeFilterController", "Selected category filter: $selectedCategory")
            onFilterChanged()
        }
    }

    fun observeCategories(binding: FragmentHomeBinding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                categoryRepository.observeCategories().collect { categories ->
                    Logger.d("HomeFilterController", "Categories updated: count=${categories.size}")
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
                            Logger.d("HomeFilterController", "Category filter selected id: $selectedCategoryFilterId")
                            renderClips(getLatestClips())
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                }
            }
        }
    }
}
