package com.inscopelabs.abx.clipinbox.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inscopelabs.abx.clipinbox.ClipInBoxApplication
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.databinding.FragmentHomeBinding
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.domain.ClipRepository
import com.inscopelabs.abx.clipinbox.export.FileExporter
import com.inscopelabs.abx.clipinbox.utils.ClipboardHelper
import com.inscopelabs.abx.clipinbox.utils.NotificationPreferences
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import com.inscopelabs.abx.clipinbox.category.CategoryRepository

class HomeFragment : Fragment(), ClipListAdapter.Listener, ClipActionBottomSheet.Callback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ClipRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var adapter: ClipListAdapter

    private var searchQuery = ""
    private var selectedCategory = "All"
    private var selectedCategoryFilterId: Long? = null
    private var collectJob: Job? = null
    private var latestClips: List<ClipEntity> = emptyList()

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        if (uri != null) {
            val clipsToExport = if (adapter.isSelectionMode()) {
                adapter.getSelectedClips()
            } else {
                latestClips
            }

            if (clipsToExport.isNotEmpty()) {
                try {
                    FileExporter.exportAsTxt(requireContext(), clipsToExport, uri)
                    showMessage(getString(R.string.home_export_success_format, clipsToExport.size))
                    if (adapter.isSelectionMode()) {
                        adapter.clearSelection()
                    }
                } catch (e: Exception) {
                    Logger.e("HomeFragment", "Export failed", e)
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
        Logger.d("HomeFragment", "onCreateView")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d("HomeFragment", "onViewCreated")

        val app = requireActivity().application as ClipInBoxApplication
        repository = app.repository
        categoryRepository = app.categoryRepository

        setupRecyclerView()
        setupSwipeGestures()
        setupSearch()
        setupCategoryChips()
        setupCaptureButton()
        setupNotificationToggle()
        setupFab()
        setupSelectionBar()

        requireActivity().addMenuProvider(object : androidx.core.view.MenuProvider {
            override fun onCreateMenu(menu: android.view.Menu, menuInflater: android.view.MenuInflater) {
                menuInflater.inflate(R.menu.inbox_toolbar_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_export_txt -> { exportTxt(); true }
                    R.id.action_clear_unpinned -> { clearUnpinned(); true }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        observeCategories()
        observeClips()
    }

    private fun setupRecyclerView() {
        adapter = ClipListAdapter(this)
        binding.recyclerViewClips.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewClips.adapter = adapter
    }

    private fun setupSwipeGestures() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                if (viewHolder is ClipListAdapter.HeaderViewHolder) return 0
                return super.getSwipeDirs(recyclerView, viewHolder)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val item = adapter.currentList.getOrNull(position) as? ClipListItem.Clip ?: return
                val clip = item.clip

                if (direction == ItemTouchHelper.RIGHT) {
                    lifecycleScope.launch {
                        repository.archiveClip(clip)
                        Logger.i("HomeFragment", "Clip ${clip.id} archived via swipe right")
                        val view = _binding?.root ?: return@launch
                        Snackbar.make(view, getString(R.string.home_toast_clip_archived), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.home_action_undo)) {
                                lifecycleScope.launch {
                                    repository.updateClip(clip.copy(isArchived = false))
                                    Logger.i("HomeFragment", "Clip ${clip.id} restored from archive")
                                }
                            }
                            .show()
                    }
                } else if (direction == ItemTouchHelper.LEFT) {
                    lifecycleScope.launch {
                        repository.deleteClip(clip)
                        Logger.i("HomeFragment", "Clip ${clip.id} deleted via swipe left")
                        val view = _binding?.root ?: return@launch
                        Snackbar.make(view, getString(R.string.home_toast_clip_deleted), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.home_action_undo)) {
                                lifecycleScope.launch {
                                    repository.updateClip(clip)
                                    Logger.i("HomeFragment", "Clip ${clip.id} restored from deletion")
                                }
                            }
                            .show()
                    }
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val background = ColorDrawable()
                val archiveIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_archive)
                val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)

                if (dX > 0) {
                    background.color = ContextCompat.getColor(requireContext(), R.color.pastel_green_archive)
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                    background.draw(c)

                    archiveIcon?.let { icon ->
                        val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
                        val iconTop = itemView.top + iconMargin
                        val iconBottom = iconTop + icon.intrinsicHeight
                        val iconLeft = itemView.left + iconMargin
                        val iconRight = itemView.left + iconMargin + icon.intrinsicWidth
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        icon.draw(c)
                    }
                } else if (dX < 0) {
                    background.color = ContextCompat.getColor(requireContext(), R.color.pastel_red_delete)
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    background.draw(c)

                    deleteIcon?.let { icon ->
                        val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
                        val iconTop = itemView.top + iconMargin
                        val iconBottom = iconTop + icon.intrinsicHeight
                        val iconRight = itemView.right - iconMargin
                        val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        icon.draw(c)
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.recyclerViewClips)
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
            Logger.d("HomeFragment", "Selected category filter: $selectedCategory")
            observeClips()
        }
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                categoryRepository.observeCategories().collect { categories ->
                    adapter.updateCategoryColors(categories.associate { it.id to it.colorHex })

                    val spinnerItems = listOf(getString(R.string.category_all_filter)) + categories.map { it.name }
                    val spinnerAdapter = android.widget.ArrayAdapter(
                        requireContext(),
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

                    binding.spinnerCategoryFilter.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                            selectedCategoryFilterId = if (position == 0) null else categories.getOrNull(position - 1)?.id
                            renderClips(latestClips)
                        }
                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                    }
                }
            }
        }
    }

    private fun setupCaptureButton() {
        binding.btnCaptureClipboard.setOnClickListener {
            val text = ClipboardHelper.getPrimaryClipText(requireContext())
            if (text.isNullOrBlank()) {
                showMessage(getString(R.string.home_toast_clipboard_empty))
            } else {
                lifecycleScope.launch {
                    val clipId = repository.saveClipText(text)
                    if (clipId != null) {
                        showMessage(getString(R.string.home_toast_clipboard_captured))
                        val app = requireActivity().application as ClipInBoxApplication
                        CategoryPickerDialogHelper.showIfEnabledAfterSave(
                            requireContext(),
                            lifecycleScope,
                            app.categoryRepository,
                            repository,
                            clipId
                        )
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
        binding.btnCancelSelection.setOnClickListener {
            adapter.clearSelection()
        }
        binding.btnActionCopy.setOnClickListener {
            val selectedClips = adapter.getSelectedClips()
            if (selectedClips.isEmpty()) {
                showMessage(getString(R.string.home_toast_no_clips_selected))
                return@setOnClickListener
            }
            val textToCopy = selectedClips.first().content
            ClipboardHelper.copyToClipboard(requireContext(), textToCopy)
            showMessage(getString(R.string.home_toast_copied_to_clipboard))
        }
        binding.btnActionJoin.setOnClickListener {
            val selectedClips = adapter.getSelectedClips()
            if (selectedClips.size < 2) {
                showMessage(getString(R.string.home_toast_select_at_least_two))
                return@setOnClickListener
            }
            JoinClipsDialogHelper.show(requireContext(), selectedClips.size) { separator, deleteOriginals ->
                lifecycleScope.launch {
                    val sortedClips = selectedClips.sortedBy { it.timestamp }
                    val joinedText = com.inscopelabs.abx.clipinbox.utility.ClipJoiner.join(sortedClips.map { it.content }, separator)
                    val joinedId = repository.saveClipText(joinedText)
                    if (joinedId != null) {
                        repository.getClipById(joinedId)?.let { newClip ->
                            repository.updateClip(newClip.copy(categoryId = sortedClips.first().categoryId, tags = sortedClips.first().tags))
                        }
                    }
                    if (deleteOriginals) {
                        for (clip in selectedClips) {
                            repository.deleteClip(clip)
                        }
                    }
                    adapter.clearSelection()
                    showMessage(getString(R.string.home_toast_clips_joined))
                }
            }
        }
        binding.btnActionDelete.setOnClickListener {
            val selectedClips = adapter.getSelectedClips()
            if (selectedClips.isEmpty()) return@setOnClickListener
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.storage_delete_confirm)
                .setMessage(getString(R.string.home_toast_deleted_clips_format, selectedClips.size))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    lifecycleScope.launch {
                        for (clip in selectedClips) {
                            repository.deleteClip(clip)
                        }
                        adapter.clearSelection()
                        showMessage(getString(R.string.home_toast_deleted_clips_format, selectedClips.size))
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
        binding.btnActionSave.setOnClickListener {
            val ids = adapter.getSelectedIds().toLongArray()
            if (ids.isEmpty()) {
                showMessage(getString(R.string.home_toast_no_clips_selected))
                return@setOnClickListener
            }
            SaveToPathBottomSheet.newInstance(ids)
                .show(parentFragmentManager, "save_to_path")
        }
    }

    private fun observeClips() {
        collectJob?.cancel()
        collectJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val flow = when {
                    searchQuery.isNotBlank() -> repository.searchClips(searchQuery)
                    selectedCategory == "Favorites" -> repository.getFavoriteClips()
                    selectedCategory != "All" -> repository.getClipsByDetectedType(selectedCategory)
                    else -> repository.getInboxClips()
                }

                flow.collectLatest { clips ->
                    renderClips(clips)
                }
            }
        }
    }

    private fun renderClips(rawClips: List<ClipEntity>) {
        latestClips = rawClips
        val displayClips = selectedCategoryFilterId?.let { id ->
            rawClips.filter { it.categoryId == id }
        } ?: rawClips

        adapter.submitClips(displayClips)
        binding.recyclerViewClips.isVisible = displayClips.isNotEmpty()
        binding.layoutEmptyState.isVisible = displayClips.isEmpty()

        if (displayClips.isEmpty()) {
            binding.tvEmptyMessage.text = if (searchQuery.isNotBlank()) {
                getString(R.string.home_empty_matching_format, searchQuery)
            } else {
                getString(R.string.home_empty_no_clips_saved)
            }
        }
    }

    fun exportTxt() {
        if (latestClips.isEmpty()) {
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
        lifecycleScope.launch {
            repository.markRead(clip)
        }
        val sheet = ClipActionBottomSheet.newInstance(clip, this)
        sheet.show(childFragmentManager, ClipActionBottomSheet.TAG)
    }

    override fun onClipLongClick(clip: ClipEntity) {
    }

    override fun onSelectionChanged(selectedCount: Int) {
        if (selectedCount > 0) {
            binding.llContextualBar.isVisible = true
            binding.tvSelectionCount.text = getString(R.string.home_selection_count_format, selectedCount)
            binding.fabAddClip.isVisible = false
        } else {
            binding.llContextualBar.isVisible = false
            binding.fabAddClip.isVisible = true
            adapter.clearSelection()
        }
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
            val clipId = repository.saveClipText(text)
            if (clipId != null) {
                showMessage(getString(R.string.home_toast_clip_saved))
                val app = requireActivity().application as ClipInBoxApplication
                CategoryPickerDialogHelper.showIfEnabledAfterSave(
                    requireContext(),
                    lifecycleScope,
                    app.categoryRepository,
                    repository,
                    clipId
                )
            } else {
                showMessage(getString(R.string.home_toast_clip_exists))
            }
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

    override fun onUpdateClipCategory(clip: ClipEntity, categoryId: Long, tags: String) {
        lifecycleScope.launch {
            repository.updateClip(clip.copy(categoryId = categoryId, tags = tags))
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

    override fun onSplitClip(clip: ClipEntity, parts: List<String>, deleteOriginal: Boolean) {
        lifecycleScope.launch {
            for (part in parts) {
                val id = repository.saveClipText(part)
                if (id != null) {
                    repository.getClipById(id)?.let { newClip ->
                        repository.updateClip(newClip.copy(categoryId = clip.categoryId, tags = clip.tags))
                    }
                }
            }
            if (deleteOriginal) {
                repository.deleteClip(clip)
            }
            showMessage(getString(R.string.home_toast_clip_split, parts.size))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
