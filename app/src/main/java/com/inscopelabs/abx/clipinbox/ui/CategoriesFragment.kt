package com.inscopelabs.abx.clipinbox.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inscopelabs.abx.clipinbox.ClipInBoxApplication
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.category.CategoryRepository
import com.inscopelabs.abx.clipinbox.data.local.CategoryEntity
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import kotlinx.coroutines.launch

class CategoriesFragment : Fragment(), CategoryAdapter.OnCategoryClickListener {

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var adapter: CategoryAdapter

    private lateinit var rvCategories: RecyclerView
    private lateinit var btnAddCategory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i(TAG, "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.i(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_categories, container, false)

        rvCategories = view.findViewById(R.id.rv_categories)
        btnAddCategory = view.findViewById(R.id.btn_add_category)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated: binding adapter and setting up listeners")

        categoryRepository = (requireActivity().application as ClipInBoxApplication).categoryRepository

        adapter = CategoryAdapter(this)
        rvCategories.layoutManager = LinearLayoutManager(requireContext())
        rvCategories.adapter = adapter

        btnAddCategory.setOnClickListener {
            Logger.i(TAG, "btnAddCategory clicked")
            showAddCategoryDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                categoryRepository.observeCategories().collect { categories ->
                    Logger.d(TAG, "Observed ${categories.size} categories")
                    adapter.submitList(categories)
                }
            }
        }
    }

    private fun showAddCategoryDialog() {
        val context = requireContext()
        val etName = EditText(context).apply {
            hint = getString(R.string.category_name_hint)
        }

        var selectedColor = COLOR_PALETTE.first()
        val colorPickerLayout = buildColorPickerLayout(context, selectedColor) { hex ->
            selectedColor = hex
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
            addView(etName)
            addView(colorPickerLayout)
        }

        AlertDialog.Builder(context)
            .setTitle(R.string.category_add)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isBlank()) {
                    Toast.makeText(context, R.string.category_name_hint, Toast.LENGTH_SHORT).show()
                    Logger.w(TAG, "Add category failed: blank name")
                } else {
                    lifecycleScope.launch {
                        val id = categoryRepository.addCategory(name, selectedColor)
                        Logger.i(TAG, "Added category '$name' with color $selectedColor, new id=$id")
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onEditCategory(category: CategoryEntity) {
        Logger.i(TAG, "onEditCategory requested for id=${category.id}, name='${category.name}'")
        val context = requireContext()
        val etName = EditText(context).apply {
            hint = getString(R.string.category_name_hint)
            setText(category.name)
        }

        var selectedColor = category.colorHex
        val colorPickerLayout = buildColorPickerLayout(context, selectedColor) { hex ->
            selectedColor = hex
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
            addView(etName)
            addView(colorPickerLayout)
        }

        AlertDialog.Builder(context)
            .setTitle(category.name)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val newName = etName.text.toString().trim()
                if (newName.isBlank()) {
                    Toast.makeText(context, R.string.category_name_hint, Toast.LENGTH_SHORT).show()
                    Logger.w(TAG, "Edit category failed: blank name")
                } else {
                    lifecycleScope.launch {
                        categoryRepository.updateCategory(category.copy(name = newName, colorHex = selectedColor))
                        Logger.i(TAG, "Updated category id=${category.id} name='$newName' color=$selectedColor")
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onSetDefault(category: CategoryEntity) {
        Logger.i(TAG, "onSetDefault requested for id=${category.id}, name='${category.name}', isDefault=${category.isDefault}")
        if (category.isDefault) {
            Logger.d(TAG, "Category id=${category.id} is already default, ignoring")
            return
        }
        lifecycleScope.launch {
            categoryRepository.setDefaultCategory(category)
            Logger.i(TAG, "Set category id=${category.id} as default")
        }
    }

    override fun onDeleteCategory(category: CategoryEntity) {
        Logger.i(TAG, "onDeleteCategory requested for id=${category.id}, name='${category.name}'")
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.storage_delete_confirm)
            .setMessage(category.name)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                lifecycleScope.launch {
                    val deleted = categoryRepository.deleteCategory(category)
                    if (!deleted) {
                        Logger.w(TAG, "Cannot delete default category id=${category.id}")
                        Toast.makeText(requireContext(), R.string.category_cannot_delete_default, Toast.LENGTH_LONG).show()
                    } else {
                        Logger.i(TAG, "Successfully deleted category id=${category.id}")
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun buildColorPickerLayout(
        context: Context,
        initialColorHex: String,
        onColorSelected: (String) -> Unit
    ): LinearLayout {
        var activeColor = initialColorHex
        if (!COLOR_PALETTE.any { it.equals(activeColor, ignoreCase = true) }) {
            activeColor = COLOR_PALETTE.first()
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 24, 0, 8)
        }

        val density = context.resources.displayMetrics.density
        val sizePx = (28 * density).toInt()
        val marginPx = (4 * density).toInt()

        val swatchViews = mutableListOf<View>()

        fun updateUI() {
            COLOR_PALETTE.forEachIndexed { index, hex ->
                val view = swatchViews.getOrNull(index) ?: return@forEachIndexed
                val isSelected = hex.equals(activeColor, ignoreCase = true)
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(hex))
                    if (isSelected) {
                        setStroke((3 * density).toInt(), Color.parseColor("#1A1C1E"))
                    } else {
                        setStroke(0, Color.TRANSPARENT)
                    }
                }
                view.background = drawable
            }
        }

        COLOR_PALETTE.forEach { hex ->
            val swatch = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                    setMargins(marginPx, marginPx, marginPx, marginPx)
                }
                setOnClickListener {
                    activeColor = hex
                    onColorSelected(hex)
                    updateUI()
                }
            }
            swatchViews.add(swatch)
            container.addView(swatch)
        }

        updateUI()
        return container
    }

    companion object {
        private const val TAG = "CategoriesFragment"
        val COLOR_PALETTE = listOf(
            "#5B6EE8",
            "#0284C7",
            "#10B981",
            "#F59E0B",
            "#EF4444",
            "#8B5CF6",
            "#EC4899",
            "#6B7280"
        )
    }
}
