package com.inscopelabs.abx.clipinbox.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.inscopelabs.abx.clipinbox.ClipInBoxApplication
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.databinding.FragmentHomeBinding
import com.inscopelabs.abx.clipinbox.domain.ClipRepository
import com.inscopelabs.abx.clipinbox.export.FileExporter
import com.inscopelabs.abx.clipinbox.utils.ClipboardHelper
import com.inscopelabs.abx.clipinbox.utils.NotificationPreferences
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), ClipListAdapter.Listener, ClipActionBottomSheet.Callback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ClipRepository
    private lateinit var adapter: ClipListAdapter

    private var searchQuery = ""
    private var selectedCategory = "All"
    private var collectJob: Job? = null

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        if (uri != null) {
            val clipsToExport = if (adapter.isSelectionMode()) {
                adapter.getSelectedClips()
            } else {
                adapter.currentList
            }

            if (clipsToExport.isNotEmpty()) {
                try {
                    FileExporter.exportAsTxt(requireContext(), clipsToExport, uri)
                    showMessage(getString(R.string.home_export_success_format, clipsToExport.size))
                    if (adapter.isSelectionMode()) {
                        adapter.clearSelection()
                    }
                } catch (e: Exception) {
                    showMessage(getString(R.string.home_export_failed_format, e.message.orEmpty()))
                }
            } else {
                showMessage(getString(R.string.home_export_empty))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as ClipInBoxApplication
        repository = app.repository

        setupRecyclerView()
        setupSearch()
        setupCategoryChips()
        setupCaptureButton()
        setupNotificationToggle()
        setupFab()
        setupSelectionBar()

        observeClips()
    }

    private fun setupRecyclerView() {
        adapter = ClipListAdapter(this)
        binding.recyclerViewClips.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewClips.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim().orEmpty()
                observeClips()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull()
            selectedCategory = when (checkedId) {
                R.id.chip_favorites -> "Favorites"
                R.id.chip_text -> "Text"
                R.id.chip_link -> "Link"
                R.id.chip_code -> "Code"
                R.id.chip_note -> "Note"
                else -> "All"
            }
            observeClips()
        }
    }

    private fun setupCaptureButton() {
        binding.btnCaptureClipboard.setOnClickListener {
            val text = ClipboardHelper.getPrimaryClipText(requireContext())
            if (text.isNullOrBlank()) {
                showMessage(getString(R.string.home_toast_clipboard_empty))
            } else {
                lifecycleScope.launch {
                    val saved = repository.saveClipText(text)
                    if (saved) {
                        showMessage(getString(R.string.home_toast_clipboard_captured))
                    } else {
                        showMessage(getString(R.string.home_toast_clip_exists))
                    }
                }
            }
        }
    }

    private fun setupNotificationToggle() {
        val isEnabled = NotificationPreferences.isPersistentNotificationEnabled(requireContext())
        binding.switchPinNotification.isChecked = isEnabled

        binding.switchPinNotification.setOnCheckedChangeListener { _, isChecked ->
            val app = requireActivity().application as ClipInBoxApplication
            app.setNotificationTriggerEnabled(isChecked)
            if (isChecked) {
                showMessage(getString(R.string.home_toast_notification_pinned))
            } else {
                showMessage(getString(R.string.home_toast_notification_unpinned))
            }
        }
    }

    private fun setupFab() {
        binding.fabAddClip.setOnClickListener {
            val sheet = ClipActionBottomSheet.newInstance(null, this)
            sheet.show(childFragmentManager, ClipActionBottomSheet.TAG)
        }
        binding.btnAddFirstClip.setOnClickListener {
            val sheet = ClipActionBottomSheet.newInstance(null, this)
            sheet.show(childFragmentManager, ClipActionBottomSheet.TAG)
        }
    }

    private fun setupSelectionBar() {
        binding.btnCloseSelection.setOnClickListener {
            adapter.clearSelection()
        }
        binding.btnExportSelected.setOnClickListener {
            val selectedClips = adapter.getSelectedClips()
            if (selectedClips.isEmpty()) {
                showMessage(getString(R.string.home_toast_no_clips_selected))
                return@setOnClickListener
            }
            val timestamp = System.currentTimeMillis()
            val filename = "clipinbox-selected-export-$timestamp.txt"
            createDocumentLauncher.launch(filename)
        }
        binding.btnDeleteSelected.setOnClickListener {
            val selectedClips = adapter.getSelectedClips()
            if (selectedClips.isEmpty()) return@setOnClickListener
            lifecycleScope.launch {
                selectedClips.forEach { repository.deleteClip(it) }
                adapter.clearSelection()
                showMessage(getString(R.string.home_toast_deleted_clips_format, selectedClips.size))
            }
        }
    }

    private fun observeClips() {
        collectJob?.cancel()
        collectJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val flow = when {
                    searchQuery.isNotBlank() -> repository.searchClips(searchQuery)
                    selectedCategory == "Favorites" -> repository.getFavoriteClips()
                    selectedCategory != "All" -> repository.getClipsByCategory(selectedCategory)
                    else -> repository.getAllClips()
                }

                flow.collectLatest { clips ->
                    adapter.submitList(clips)
                    binding.recyclerViewClips.isVisible = clips.isNotEmpty()
                    binding.layoutEmptyState.isVisible = clips.isEmpty()

                    if (clips.isEmpty()) {
                        binding.tvEmptyMessage.text = if (searchQuery.isNotBlank()) {
                            getString(R.string.home_empty_matching_format, searchQuery)
                        } else {
                            getString(R.string.home_empty_no_clips_saved)
                        }
                    }
                }
            }
        }
    }

    fun exportTxt() {
        val currentClips = adapter.currentList
        if (currentClips.isEmpty()) {
            showMessage(getString(R.string.home_export_no_clips_available))
            return
        }
        val timestamp = System.currentTimeMillis()
        val filename = "clipinbox-export-$timestamp.txt"
        createDocumentLauncher.launch(filename)
    }

    fun clearUnpinned() {
        lifecycleScope.launch {
            repository.clearUnpinned()
            showMessage(getString(R.string.home_toast_cleared_unpinned))
        }
    }

    private fun showMessage(msg: String) {
        val view = _binding?.root ?: return
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show()
    }

    private fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.home_share_chooser_title)))
    }

    override fun onClipClick(clip: ClipEntity) {
        val sheet = ClipActionBottomSheet.newInstance(clip, this)
        sheet.show(childFragmentManager, ClipActionBottomSheet.TAG)
    }

    override fun onClipLongClick(clip: ClipEntity) {
    }

    override fun onSelectionChanged(selectedCount: Int) {
        binding.tvSelectionCount.text = getString(R.string.home_selection_count_format, selectedCount)
        val inSelectionMode = selectedCount > 0
        binding.layoutSelectionBar.isVisible = inSelectionMode
        binding.fabAddClip.isVisible = !inSelectionMode
    }

    override fun onCopyClick(clip: ClipEntity) {
        ClipboardHelper.copyToClipboard(requireContext(), clip.content)
        showMessage(getString(R.string.home_toast_copied_to_clipboard))
    }

    override fun onPinClick(clip: ClipEntity) {
        lifecycleScope.launch {
            val updated = clip.copy(isPinned = !clip.isPinned)
            repository.updateClip(updated)
            showMessage(getString(if (updated.isPinned) R.string.home_toast_clip_pinned else R.string.home_toast_clip_unpinned))
        }
    }

    override fun onFavoriteClick(clip: ClipEntity) {
        lifecycleScope.launch {
            val updated = clip.copy(isFavorite = !clip.isFavorite)
            repository.updateClip(updated)
        }
    }

    override fun onShareClick(clip: ClipEntity) {
        shareText(requireContext(), clip.content)
    }

    override fun onDeleteClick(clip: ClipEntity) {
        lifecycleScope.launch {
            repository.deleteClip(clip)
            showMessage(getString(R.string.home_toast_clip_deleted))
        }
    }

    override fun onSaveNewClip(text: String) {
        lifecycleScope.launch {
            repository.saveClipText(text)
            showMessage(getString(R.string.home_toast_clip_saved))
        }
    }

    override fun onUpdateClip(clip: ClipEntity, newContent: String) {
        lifecycleScope.launch {
            val updated = clip.copy(
                content = newContent,
                charCount = newContent.length,
                wordCount = if (newContent.isBlank()) 0 else newContent.trim().split("\\s+".toRegex()).size
            )
            repository.updateClip(updated)
            showMessage(getString(R.string.home_toast_clip_updated))
        }
    }

    override fun onShareClip(clip: ClipEntity) {
        shareText(requireContext(), clip.content)
    }

    override fun onCopyClip(clip: ClipEntity) {
        ClipboardHelper.copyToClipboard(requireContext(), clip.content)
        showMessage(getString(R.string.home_toast_copied_to_clipboard))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
