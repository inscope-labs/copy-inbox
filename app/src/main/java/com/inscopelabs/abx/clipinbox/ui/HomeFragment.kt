package com.inscopelabs.abx.clipinbox.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.inscopelabs.abx.clipinbox.ClipInBoxApplication
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.databinding.FragmentHomeBinding
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.export.FileExporter
import com.inscopelabs.abx.clipinbox.utils.ClipboardHelper
import com.inscopelabs.abx.clipinbox.utils.NotificationPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), ClipListAdapter.Listener, ClipActionBottomSheet.Callback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ClipListAdapter
    private lateinit var filterController: HomeFilterController
    private lateinit var selectionBarController: HomeSelectionBarController
    private lateinit var actionHandler: HomeClipActionHandler

    private var collectJob: Job? = null
    private var latestClips: List<ClipEntity> = emptyList()

    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri: Uri? ->
        if (uri != null) {
            val clipsToExport = if (adapter.isSelectionMode()) adapter.getSelectedClips() else latestClips
            if (clipsToExport.isNotEmpty()) {
                try {
                    FileExporter.exportAsTxt(requireContext(), clipsToExport, uri)
                    showMessage(getString(R.string.home_export_success_format, clipsToExport.size))
                    if (adapter.isSelectionMode()) adapter.clearSelection()
                } catch (e: Exception) {
                    Logger.e("HomeFragment", "Export failed", e)
                    showMessage(getString(R.string.home_export_failed_format, e.message.orEmpty()))
                }
            } else showMessage(getString(R.string.home_export_empty))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.d("HomeFragment", "onCreateView")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d("HomeFragment", "onViewCreated")

        val app = requireActivity().application as ClipInBoxApplication
        adapter = ClipListAdapter(this)
        binding.recyclerViewClips.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewClips.adapter = adapter

        filterController = HomeFilterController(requireContext(), viewLifecycleOwner, app.categoryRepository, adapter, { observeClips(app) }, { latestClips }, { renderClips(it) })
        selectionBarController = HomeSelectionBarController(requireContext(), lifecycleScope, app.repository, adapter, parentFragmentManager) { showMessage(it) }
        actionHandler = HomeClipActionHandler(requireContext(), lifecycleScope, app.repository, app.categoryRepository, childFragmentManager, { showMessage(it) }, { shareText(it) })

        ItemTouchHelper(ClipSwipeCallback(requireContext(), lifecycleScope, app.repository, adapter) { _binding?.root }).attachToRecyclerView(binding.recyclerViewClips)

        filterController.setupSearch(binding)
        filterController.setupCategoryChips(binding)
        selectionBarController.setupSelectionBar(binding)

        binding.btnCaptureClipboard.setOnClickListener {
            val text = ClipboardHelper.getPrimaryClipText(requireContext())
            if (text.isNullOrBlank()) showMessage(getString(R.string.home_toast_clipboard_empty)) else actionHandler.onSaveNewClip(text)
        }

        binding.switchPinNotification.isChecked = NotificationPreferences.isPersistentNotificationEnabled(requireContext())
        binding.switchPinNotification.setOnCheckedChangeListener { _, isChecked ->
            app.setNotificationTriggerEnabled(isChecked)
            showMessage(getString(if (isChecked) R.string.home_toast_notification_pinned else R.string.home_toast_notification_unpinned))
        }

        val openAddSheet = { ClipActionBottomSheet.newInstance(null, this).show(childFragmentManager, ClipActionBottomSheet.TAG) }
        binding.fabAddClip.setOnClickListener { openAddSheet() }
        binding.btnAddFirstClip.setOnClickListener { openAddSheet() }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: android.view.Menu, menuInflater: android.view.MenuInflater) = menuInflater.inflate(R.menu.inbox_toolbar_menu, menu)
            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean = when (menuItem.itemId) {
                R.id.action_export_txt -> { exportTxt(); true }
                R.id.action_clear_unpinned -> { actionHandler.clearUnpinned(); true }
                else -> false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        filterController.observeCategories(binding)
        observeClips(app)
    }

    private fun observeClips(app: ClipInBoxApplication) {
        collectJob?.cancel()
        collectJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val sq = filterController.searchQuery
                val sc = filterController.selectedCategory
                val flow = when {
                    sq.isNotBlank() -> app.repository.searchClips(sq)
                    sc == "Favorites" -> app.repository.getFavoriteClips()
                    sc != "All" -> app.repository.getClipsByDetectedType(sc)
                    else -> app.repository.getInboxClips()
                }
                flow.collectLatest { renderClips(it) }
            }
        }
    }

    private fun renderClips(rawClips: List<ClipEntity>) {
        latestClips = rawClips
        val catId = filterController.selectedCategoryFilterId
        val displayClips = if (catId != null) rawClips.filter { it.categoryId == catId } else rawClips

        adapter.submitClips(displayClips)
        binding.recyclerViewClips.isVisible = displayClips.isNotEmpty()
        binding.layoutEmptyState.isVisible = displayClips.isEmpty()

        if (displayClips.isEmpty()) {
            val sq = filterController.searchQuery
            binding.tvEmptyMessage.text = if (sq.isNotBlank()) getString(R.string.home_empty_matching_format, sq) else getString(R.string.home_empty_no_clips_saved)
        }
    }

    fun exportTxt() = if (latestClips.isEmpty()) showMessage(getString(R.string.home_export_no_clips_available)) else createDocumentLauncher.launch("clipinbox-export-${System.currentTimeMillis()}.txt")

    private fun showMessage(msg: String) {
        _binding?.root?.let { Snackbar.make(it, msg, Snackbar.LENGTH_SHORT).show() }
    }

    private fun shareText(text: String) {
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }, getString(R.string.home_share_chooser_title)))
    }

    override fun onClipClick(clip: ClipEntity) = actionHandler.onClipClick(clip, this)
    override fun onClipLongClick(clip: ClipEntity) = Unit
    override fun onSelectionChanged(selectedCount: Int) = selectionBarController.onSelectionChanged(binding, selectedCount)
    override fun onCopyClick(clip: ClipEntity) = actionHandler.onCopyClick(clip)
    override fun onPinClick(clip: ClipEntity) = actionHandler.onPinClick(clip)
    override fun onFavoriteClick(clip: ClipEntity) = actionHandler.onFavoriteClick(clip)
    override fun onShareClick(clip: ClipEntity) = actionHandler.onShareClick(clip)
    override fun onDeleteClick(clip: ClipEntity) = actionHandler.onDeleteClick(clip)
    override fun onSaveNewClip(text: String) = actionHandler.onSaveNewClip(text)
    override fun onUpdateClip(clip: ClipEntity, newContent: String) = actionHandler.onUpdateClip(clip, newContent)
    override fun onUpdateClipCategory(clip: ClipEntity, categoryId: Long, tags: String) = actionHandler.onUpdateClipCategory(clip, categoryId, tags)
    override fun onShareClip(clip: ClipEntity) = actionHandler.onShareClip(clip)
    override fun onCopyClip(clip: ClipEntity) = actionHandler.onCopyClip(clip)
    override fun onSplitClip(clip: ClipEntity, parts: List<String>, deleteOriginal: Boolean) = actionHandler.onSplitClip(clip, parts, deleteOriginal)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
