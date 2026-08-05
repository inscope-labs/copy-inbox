package com.inscopelabs.abx.clipinbox.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.inscopelabs.abx.clipinbox.ClipInBoxApplication
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.data.local.NamingMacro
import com.inscopelabs.abx.clipinbox.data.local.SafPath
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.export.saf.MacroExpander
import com.inscopelabs.abx.clipinbox.export.saf.SafExporter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText

class SaveToPathBottomSheet : BottomSheetDialogFragment() {

    private lateinit var rvPathChoices: RecyclerView
    private lateinit var spinnerMacro: Spinner
    private lateinit var etFilenamePreview: TextInputEditText
    private lateinit var btnSaveConfirm: Button

    private var selectedPath: SafPath? = null
    private var selectedMacro: NamingMacro? = null
    private var firstClip: ClipEntity? = null
    private var clipIds: LongArray = longArrayOf()
    private var isFilenameManuallyEdited = false

    private var filenameTextWatcher: TextWatcher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clipIds = arguments?.getLongArray(ARG_CLIP_IDS) ?: longArrayOf()
        Logger.i(TAG, "onCreate: received ${clipIds.size} clip ids")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.i(TAG, "onCreateView")
        return inflater.inflate(R.layout.bottom_sheet_save_to_path, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated")

        rvPathChoices = view.findViewById(R.id.rv_path_choices)
        spinnerMacro = view.findViewById(R.id.spinner_macro)
        etFilenamePreview = view.findViewById(R.id.et_filename_preview)
        btnSaveConfirm = view.findViewById(R.id.btn_save_confirm)

        if (clipIds.size > 1) {
            etFilenamePreview.isEnabled = false
        }

        filenameTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                isFilenameManuallyEdited = true
            }
        }
        filenameTextWatcher?.let { etFilenamePreview.addTextChangedListener(it) }

        rvPathChoices.layoutManager = LinearLayoutManager(requireContext())

        val app = requireActivity().application as ClipInBoxApplication
        val repo = app.safPathRepository
        val clipRepo = app.repository
        val exporter = SafExporter(requireContext())

        lifecycleScope.launch {
            if (clipIds.isNotEmpty()) {
                firstClip = clipRepo.getClipById(clipIds.first())
            }

            val paths = repo.observePaths().first()
            if (paths.isEmpty()) {
                Logger.w(TAG, "No paths saved yet. Navigating to StoragePathsFragment.")
                Toast.makeText(requireContext(), R.string.save_no_paths_message, Toast.LENGTH_SHORT).show()
                dismiss()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, StoragePathsFragment())
                    .addToBackStack("storage_paths")
                    .commit()
                return@launch
            }

            val lastUsed = repo.lastUsedPath()
            selectedPath = paths.find { it.id == lastUsed?.id } ?: paths.first()

            val pathAdapter = PathChoiceAdapter(paths, selectedPath) { clickedPath ->
                selectedPath = clickedPath
                Logger.d(TAG, "Path selected: ${clickedPath.label}")
                updatePreview()
            }
            rvPathChoices.adapter = pathAdapter

            val macros = repo.observeMacros().first()
            val macroLabels = mutableListOf("Auto-generate")
            macroLabels.addAll(macros.map { it.label })

            val spinnerAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                macroLabels
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            spinnerMacro.adapter = spinnerAdapter

            spinnerMacro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedMacro = if (position == 0) null else macros.getOrNull(position - 1)
                    Logger.d(TAG, "Macro selected pos=$position: ${selectedMacro?.label ?: "Auto-generate"}")
                    updatePreview()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            updatePreview()

            btnSaveConfirm.setOnClickListener {
                val path = selectedPath
                if (path == null) {
                    Toast.makeText(requireContext(), "Select a folder", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val treeUri = Uri.parse(path.treeUri)
                lifecycleScope.launch {
                    val clips = mutableListOf<ClipEntity>()
                    for (id in clipIds) {
                        clipRepo.getClipById(id)?.let { clips.add(it) }
                    }
                    if (clips.isEmpty()) {
                        Logger.w(TAG, "No valid clips found for save")
                        dismiss()
                        return@launch
                    }
                    val names = if (clips.size == 1) {
                        val editedName = etFilenamePreview.text?.toString()?.trim().orEmpty()
                        if (editedName.isNotBlank()) {
                            listOf(editedName)
                        } else {
                            val clip = clips.first()
                            val macro = selectedMacro
                            val fallbackName = if (macro == null) {
                                MacroExpander.defaultFilename(clip, 0)
                            } else {
                                MacroExpander.expand(macro.template, clip, path, 0)
                            }
                            Logger.w(TAG, "Edited filename was blank, falling back to macro-computed name: $fallbackName")
                            listOf(fallbackName)
                        }
                    } else {
                        clips.mapIndexed { i, clip ->
                            val macro = selectedMacro
                            if (macro == null) {
                                MacroExpander.defaultFilename(clip, i)
                            } else {
                                MacroExpander.expand(macro.template, clip, path, i)
                            }
                        }
                    }
                    Logger.i(TAG, "Saving ${clips.size} clips to treeUri=${path.treeUri}")
                    val results = exporter.saveClips(clips, treeUri, names)
                    repo.recordUse(path)

                    val successCount = results.count { it.isSuccess }
                    val msg = if (successCount == results.size) {
                        getString(R.string.save_success, successCount)
                    } else {
                        getString(R.string.save_partial_failure, successCount, results.size)
                    }
                    Logger.i(TAG, "Save complete: $successCount / ${results.size} succeeded")
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }
    }

    private fun updatePreview() {
        if (clipIds.size > 1 || !isFilenameManuallyEdited) {
            val clip = firstClip ?: return
            val path = selectedPath ?: return
            val macro = selectedMacro
            val name = if (macro == null) {
                MacroExpander.defaultFilename(clip, 0)
            } else {
                MacroExpander.expand(macro.template, clip, path, 0)
            }
            
            // Programmatic update: temporarily detach watcher so isFilenameManuallyEdited is not tripped
            filenameTextWatcher?.let { etFilenamePreview.removeTextChangedListener(it) }
            etFilenamePreview.setText(name)
            filenameTextWatcher?.let { etFilenamePreview.addTextChangedListener(it) }
        }
    }

    private class PathChoiceAdapter(
        private val paths: List<SafPath>,
        initialSelected: SafPath?,
        private val onPathSelected: (SafPath) -> Unit
    ) : RecyclerView.Adapter<PathChoiceAdapter.ViewHolder>() {

        private var selectedPath: SafPath? = initialSelected

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_path_choice, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val path = paths[position]
            val rb = holder.radioButton
            rb.text = path.label
            rb.isChecked = (path.id == selectedPath?.id)
            rb.setOnClickListener {
                selectedPath = path
                notifyDataSetChanged()
                onPathSelected(path)
            }
        }

        override fun getItemCount(): Int = paths.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val radioButton: RadioButton = itemView.findViewById(R.id.rb_path_choice)
        }
    }

    companion object {
        private const val TAG = "SaveToPathBottomSheet"
        private const val ARG_CLIP_IDS = "clip_ids"

        fun newInstance(clipIds: LongArray): SaveToPathBottomSheet {
            return SaveToPathBottomSheet().apply {
                arguments = Bundle().apply {
                    putLongArray(ARG_CLIP_IDS, clipIds)
                }
            }
        }
    }
}
