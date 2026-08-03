package com.inscopelabs.abx.clipinbox.diagnostics

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.ChipGroup
import com.inscopelabs.abx.clipinbox.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogViewerBottomSheet : BottomSheetDialogFragment() {

    private lateinit var rvLogEntries: RecyclerView
    private lateinit var etSearchLogs: EditText
    private lateinit var chipGroupLogLevels: ChipGroup
    private lateinit var tvLogCount: TextView
    private lateinit var tvEmptyState: TextView
    private lateinit var btnExport: ImageButton

    private val adapter = LogEntryListAdapter()
    private var allEntries = emptyList<LogViewerAdapter.LogEntry>()
    private var currentFilterLevel = "ALL"
    private var currentSearchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_log_viewer_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvLogEntries = view.findViewById(R.id.rvLogEntries)
        etSearchLogs = view.findViewById(R.id.etSearchLogs)
        chipGroupLogLevels = view.findViewById(R.id.chipGroupLogLevels)
        tvLogCount = view.findViewById(R.id.tvLogCount)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        btnExport = view.findViewById(R.id.btnExport)

        rvLogEntries.layoutManager = LinearLayoutManager(requireContext())
        rvLogEntries.adapter = adapter

        setupFilterListeners()
        loadLogs()
    }

    private fun setupFilterListeners() {
        btnExport.setOnClickListener {
            val bundle = DiagnosticBundle.createBundle(requireContext())
            DiagnosticExporter.shareDiagnosticBundle(requireContext(), bundle)
        }

        etSearchLogs.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        chipGroupLogLevels.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilterLevel = when {
                checkedIds.contains(R.id.chipLevelDebug) -> "DEBUG"
                checkedIds.contains(R.id.chipLevelInfo) -> "INFO"
                checkedIds.contains(R.id.chipLevelWarn) -> "WARN"
                checkedIds.contains(R.id.chipLevelError) -> "ERROR"
                else -> "ALL"
            }
            applyFilters()
        }
    }

    private fun loadLogs() {
        lifecycleScope.launch {
            val entries = withContext(Dispatchers.IO) {
                val file = Logger.getLogFile()
                if (file != null && file.exists()) {
                    LogViewerAdapter.parseLogLines(file.readLines())
                } else {
                    emptyList()
                }
            }
            allEntries = entries
            applyFilters()
        }
    }

    private fun applyFilters() {
        val filtered = LogSearchEngine.filterLogs(allEntries, currentSearchQuery, currentFilterLevel)
        adapter.submitList(filtered)
        tvLogCount.text = "Showing ${filtered.size} of ${allEntries.size} entries"
        tvEmptyState.isVisible = filtered.isEmpty()
        rvLogEntries.isVisible = filtered.isNotEmpty()
    }
}
