package com.inscopelabs.abx.clipinbox.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inscopelabs.abx.clipinbox.ClipInBoxApplication
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.data.local.NamingMacro
import com.inscopelabs.abx.clipinbox.data.local.SafPath
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.export.saf.SafPathRepository
import kotlinx.coroutines.launch

class StoragePathsFragment : Fragment(),
    SafPathAdapter.OnPathClickListener,
    NamingMacroAdapter.OnMacroClickListener {

    private lateinit var repo: SafPathRepository
    private lateinit var pathAdapter: SafPathAdapter
    private lateinit var macroAdapter: NamingMacroAdapter

    private lateinit var rvPaths: RecyclerView
    private lateinit var rvMacros: RecyclerView
    private lateinit var btnAddPath: Button
    private lateinit var btnAddMacro: Button

    private var pendingPathLabel: String = ""

    private lateinit var openTreeLauncher: ActivityResultLauncher<Uri?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i(TAG, "onCreate")

        openTreeLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { uri ->
            if (uri == null) {
                Logger.w(TAG, "OpenDocumentTree returned null URI")
                return@registerForActivityResult
            }
            Logger.i(TAG, "OpenDocumentTree granted URI: $uri for label '$pendingPathLabel'")
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val labelToSave = pendingPathLabel
            pendingPathLabel = ""
            lifecycleScope.launch {
                repo.addPath(labelToSave, uri)
                Logger.i(TAG, "Persisted SafPath '$labelToSave'")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.i(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_storage_paths, container, false)

        rvPaths = view.findViewById(R.id.rv_paths)
        rvMacros = view.findViewById(R.id.rv_macros)
        btnAddPath = view.findViewById(R.id.btn_add_path)
        btnAddMacro = view.findViewById(R.id.btn_add_macro)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated: binding adapters and setup listeners")

        repo = (requireActivity().application as ClipInBoxApplication).safPathRepository

        pathAdapter = SafPathAdapter(this)
        rvPaths.layoutManager = LinearLayoutManager(requireContext())
        rvPaths.adapter = pathAdapter

        macroAdapter = NamingMacroAdapter(this)
        rvMacros.layoutManager = LinearLayoutManager(requireContext())
        rvMacros.adapter = macroAdapter

        btnAddPath.setOnClickListener {
            Logger.i(TAG, "btnAddPath clicked")
            showAddFolderDialog()
        }

        btnAddMacro.setOnClickListener {
            Logger.i(TAG, "btnAddMacro clicked")
            showAddMacroDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    repo.observePaths().collect { paths ->
                        Logger.d(TAG, "Observed ${paths.size} SafPaths")
                        pathAdapter.submitList(paths)
                    }
                }
                launch {
                    repo.observeMacros().collect { macros ->
                        Logger.d(TAG, "Observed ${macros.size} NamingMacros")
                        macroAdapter.submitList(macros)
                    }
                }
            }
        }
    }

    private fun showAddFolderDialog() {
        val context = requireContext()
        val input = EditText(context).apply {
            hint = getString(R.string.storage_macro_label_hint)
        }
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
            addView(input)
        }

        AlertDialog.Builder(context)
            .setTitle(R.string.storage_add_folder)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val label = input.text.toString().trim()
                if (label.isBlank()) {
                    Toast.makeText(context, R.string.storage_macro_label_hint, Toast.LENGTH_SHORT).show()
                } else {
                    pendingPathLabel = label
                    Logger.i(TAG, "Launching OpenDocumentTree for folder '$label'")
                    openTreeLauncher.launch(null)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showAddMacroDialog() {
        val context = requireContext()
        val etLabel = EditText(context).apply {
            hint = getString(R.string.storage_macro_label_hint)
        }
        val etTemplate = EditText(context).apply {
            hint = getString(R.string.storage_macro_template_hint)
        }
        val tvTokensHelp = TextView(context).apply {
            text = getString(R.string.storage_macro_tokens_help)
            textSize = 12f
            setPadding(0, 12, 0, 0)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
            addView(etLabel)
            addView(etTemplate)
            addView(tvTokensHelp)
        }

        AlertDialog.Builder(context)
            .setTitle(R.string.storage_add_macro)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val label = etLabel.text.toString().trim()
                val template = etTemplate.text.toString().trim()
                if (label.isBlank() || template.isBlank()) {
                    Toast.makeText(context, R.string.storage_macro_label_hint, Toast.LENGTH_SHORT).show()
                } else {
                    lifecycleScope.launch {
                        repo.addMacro(label, template)
                        Logger.i(TAG, "Added NamingMacro '$label' -> '$template'")
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDeletePath(path: SafPath) {
        Logger.i(TAG, "onDeletePath requested for id=${path.id}, label='${path.label}'")
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.storage_delete_confirm)
            .setMessage(path.label)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                lifecycleScope.launch {
                    repo.deletePath(path)
                    Logger.i(TAG, "Deleted SafPath id=${path.id}")
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onEditMacro(macro: NamingMacro) {
        Logger.i(TAG, "onEditMacro requested for id=${macro.id}, label='${macro.label}'")
        val context = requireContext()
        val etLabel = EditText(context).apply {
            hint = getString(R.string.storage_macro_label_hint)
            setText(macro.label)
        }
        val etTemplate = EditText(context).apply {
            hint = getString(R.string.storage_macro_template_hint)
            setText(macro.template)
        }
        val tvTokensHelp = TextView(context).apply {
            text = getString(R.string.storage_macro_tokens_help)
            textSize = 12f
            setPadding(0, 12, 0, 0)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
            addView(etLabel)
            addView(etTemplate)
            addView(tvTokensHelp)
        }

        AlertDialog.Builder(context)
            .setTitle(macro.label)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val newLabel = etLabel.text.toString().trim()
                val newTemplate = etTemplate.text.toString().trim()
                if (newLabel.isNotBlank() && newTemplate.isNotBlank()) {
                    lifecycleScope.launch {
                        repo.updateMacro(macro.copy(label = newLabel, template = newTemplate))
                        Logger.i(TAG, "Updated NamingMacro id=${macro.id}")
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDeleteMacro(macro: NamingMacro) {
        Logger.i(TAG, "onDeleteMacro requested for id=${macro.id}, label='${macro.label}'")
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.storage_delete_confirm)
            .setMessage(macro.label)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                lifecycleScope.launch {
                    repo.deleteMacro(macro)
                    Logger.i(TAG, "Deleted NamingMacro id=${macro.id}")
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        private const val TAG = "StoragePathsFragment"
    }
}
