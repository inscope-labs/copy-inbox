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
import com.inscopelabs.abx.clipinbox.data.local.TagEntity
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.tag.TagRepository
import kotlinx.coroutines.launch

class TagsFragment : Fragment(), TagAdapter.OnTagClickListener {

    private companion object {
        private const val TAG = "TagsFragment"
    }

    private lateinit var tagRepository: TagRepository
    private lateinit var adapter: TagAdapter

    private lateinit var rvTags: RecyclerView
    private lateinit var btnAddTag: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.i(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_tags, container, false)
        rvTags = view.findViewById(R.id.rv_tags)
        btnAddTag = view.findViewById(R.id.btn_add_tag)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated: initializing TagRepository and setting up listeners")

        val app = requireActivity().application as ClipInBoxApplication
        tagRepository = app.tagRepository

        adapter = TagAdapter(this)
        rvTags.layoutManager = LinearLayoutManager(requireContext())
        rvTags.adapter = adapter

        btnAddTag.setOnClickListener {
            Logger.i(TAG, "btnAddTag clicked")
            showAddTagDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tagRepository.observeTags().collect { tags ->
                    Logger.d(TAG, "Observed ${tags.size} active tags")
                    adapter.submitList(tags)
                }
            }
        }
    }

    private fun showAddTagDialog() {
        val context = requireContext()
        val etLabel = EditText(context).apply {
            hint = getString(R.string.tag_name_hint)
        }

        var selectedColor = CategoriesFragment.COLOR_PALETTE.first()
        val colorPickerLayout = buildColorPickerLayout(context, selectedColor) { hex ->
            selectedColor = hex
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
            addView(etLabel)
            addView(colorPickerLayout)
        }

        AlertDialog.Builder(context)
            .setTitle(R.string.tag_add)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val label = etLabel.text.toString().trim()
                if (label.isBlank()) {
                    Toast.makeText(context, R.string.tag_name_hint, Toast.LENGTH_SHORT).show()
                    Logger.w(TAG, "Add tag failed: blank label")
                } else {
                    lifecycleScope.launch {
                        val id = tagRepository.createTag(label, selectedColor)
                        Logger.i(TAG, "Added tag '$label' with color $selectedColor, new id=$id")
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onEditTag(tag: TagEntity) {
        Logger.i(TAG, "onEditTag requested for id=${tag.id}, label='${tag.label}'")
        if (tag.isSystemReserved) {
            Logger.w(TAG, "Editing system reserved tag ref-id=${tag.id} is disallowed")
            Toast.makeText(requireContext(), R.string.tag_cannot_delete_system, Toast.LENGTH_SHORT).show()
            return
        }

        val context = requireContext()
        val etLabel = EditText(context).apply {
            hint = getString(R.string.tag_name_hint)
            setText(tag.label)
        }

        var selectedColor = tag.colorHex
        val colorPickerLayout = buildColorPickerLayout(context, selectedColor) { hex ->
            selectedColor = hex
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
            addView(etLabel)
            addView(colorPickerLayout)
        }

        AlertDialog.Builder(context)
            .setTitle(tag.label)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val newLabel = etLabel.text.toString().trim()
                if (newLabel.isBlank()) {
                    Toast.makeText(context, R.string.tag_name_hint, Toast.LENGTH_SHORT).show()
                    Logger.w(TAG, "Edit tag failed: blank label")
                } else {
                    lifecycleScope.launch {
                        val id = tagRepository.createTag(newLabel, selectedColor)
                        tagRepository.deleteTag(tag.id)
                        Logger.i(TAG, "Updated tag id=${tag.id} -> newTag id=$id, label='$newLabel', color=$selectedColor")
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDeleteTag(tag: TagEntity) {
        Logger.i(TAG, "onDeleteTag requested for id=${tag.id}, label='${tag.label}'")
        if (tag.isSystemReserved) {
            Logger.w(TAG, "System reserved tag deletion blocked for id=${tag.id}")
            Toast.makeText(requireContext(), R.string.tag_cannot_delete_system, Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.storage_delete_confirm)
            .setMessage(tag.label)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                lifecycleScope.launch {
                    tagRepository.deleteTag(tag.id)
                    Logger.i(TAG, "Processed deleteTag for tag id=${tag.id}")
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
        val palette = CategoriesFragment.COLOR_PALETTE
        if (!palette.any { it.equals(activeColor, ignoreCase = true) }) {
            activeColor = palette.first()
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
            palette.forEachIndexed { index, hex ->
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

        palette.forEach { hex ->
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
}
