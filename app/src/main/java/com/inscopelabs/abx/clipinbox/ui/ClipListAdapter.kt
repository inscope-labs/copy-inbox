package com.inscopelabs.abx.clipinbox.ui

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.databinding.ItemClipBinding
import com.inscopelabs.abx.clipinbox.databinding.ItemSectionHeaderBinding
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.utils.TimeFormatter
import java.util.Calendar

sealed class ClipListItem {
    data class Header(val title: String) : ClipListItem()
    data class Clip(val clip: ClipEntity) : ClipListItem()
}

class ClipListAdapter(
    private val listener: Listener
) : ListAdapter<ClipListItem, RecyclerView.ViewHolder>(DiffCallback) {

    private val selectedIds = mutableSetOf<Long>()
    private var originalClips: List<ClipEntity> = emptyList()

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CLIP = 1
    }

    interface Listener {
        fun onClipClick(clip: ClipEntity)
        fun onClipLongClick(clip: ClipEntity)
        fun onCopyClick(clip: ClipEntity)
        fun onPinClick(clip: ClipEntity)
        fun onFavoriteClick(clip: ClipEntity)
        fun onShareClick(clip: ClipEntity)
        fun onDeleteClick(clip: ClipEntity)
        fun onSelectionChanged(selectedCount: Int)
    }

    fun submitClips(clips: List<ClipEntity>) {
        originalClips = clips
        val now = Calendar.getInstance()
        val todayYear = now.get(Calendar.YEAR)
        val todayDay = now.get(Calendar.DAY_OF_YEAR)

        val todayClips = mutableListOf<ClipEntity>()
        val earlierClips = mutableListOf<ClipEntity>()

        val clipCalendar = Calendar.getInstance()
        for (clip in clips) {
            clipCalendar.timeInMillis = clip.timestamp
            val isToday = clipCalendar.get(Calendar.YEAR) == todayYear &&
                    clipCalendar.get(Calendar.DAY_OF_YEAR) == todayDay
            if (isToday) {
                todayClips.add(clip)
            } else {
                earlierClips.add(clip)
            }
        }

        val items = mutableListOf<ClipListItem>()
        if (todayClips.isNotEmpty()) {
            items.add(ClipListItem.Header("Today"))
            items.addAll(todayClips.map { ClipListItem.Clip(it) })
        }
        if (earlierClips.isNotEmpty()) {
            items.add(ClipListItem.Header("Earlier"))
            items.addAll(earlierClips.map { ClipListItem.Clip(it) })
        }

        Logger.d("ClipListAdapter", "Submitting ${items.size} list items (${todayClips.size} today, ${earlierClips.size} earlier)")
        submitList(items)
    }

    fun isSelectionMode(): Boolean = selectedIds.isNotEmpty()

    fun toggleSelection(id: Long) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(id)
        } else {
            selectedIds.add(id)
        }
        notifyDataSetChanged()
        listener.onSelectionChanged(selectedIds.size)
    }

    fun getSelectedClips(): List<ClipEntity> {
        return originalClips.filter { selectedIds.contains(it.id) }
    }

    fun clearSelection() {
        if (selectedIds.isNotEmpty()) {
            selectedIds.clear()
            notifyDataSetChanged()
            listener.onSelectionChanged(0)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ClipListItem.Header -> VIEW_TYPE_HEADER
            is ClipListItem.Clip -> VIEW_TYPE_CLIP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            val binding = ItemSectionHeaderBinding.inflate(inflater, parent, false)
            HeaderViewHolder(binding)
        } else {
            val binding = ItemClipBinding.inflate(inflater, parent, false)
            ClipViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ClipListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is ClipListItem.Clip -> (holder as ClipViewHolder).bind(item.clip)
        }
    }

    class HeaderViewHolder(
        private val binding: ItemSectionHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: ClipListItem.Header) {
            binding.tvSectionHeader.text = header.title
        }
    }

    inner class ClipViewHolder(
        private val binding: ItemClipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(clip: ClipEntity) {
            val context = binding.root.context
            val isSelected = selectedIds.contains(clip.id)

            binding.ivSelectionCheck.isVisible = isSelected
            binding.cardClip.strokeWidth = if (isSelected) 6 else 2

            binding.chipCategory.text = clip.category
            binding.tvTime.text = TimeFormatter.formatRelativeTime(clip.timestamp)
            binding.tvContent.text = clip.content

            binding.viewUnreadDot.isVisible = !clip.isRead

            val iconRes = when (clip.category) {
                "Link" -> android.R.drawable.ic_menu_compass
                "Code" -> android.R.drawable.ic_menu_preferences
                "Note" -> android.R.drawable.ic_menu_edit
                "Favorites" -> android.R.drawable.btn_star_big_on
                else -> android.R.drawable.ic_menu_sort_by_size
            }
            binding.ivCategoryBadge.setImageResource(iconRes)

            if (clip.category == "Code") {
                binding.tvContent.typeface = Typeface.MONOSPACE
            } else {
                binding.tvContent.typeface = Typeface.DEFAULT
            }

            binding.tvMetaCounts.text = context.getString(
                R.string.clip_item_meta_counts_format,
                clip.charCount,
                clip.wordCount
            )

            binding.btnPin.setImageResource(
                if (clip.isPinned) android.R.drawable.ic_menu_today else android.R.drawable.ic_menu_agenda
            )

            binding.btnFavorite.setImageResource(
                if (clip.isFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
            )

            binding.cardClip.setOnClickListener {
                if (isSelectionMode()) {
                    toggleSelection(clip.id)
                } else {
                    listener.onClipClick(clip)
                }
            }

            binding.cardClip.setOnLongClickListener {
                toggleSelection(clip.id)
                listener.onClipLongClick(clip)
                true
            }

            binding.btnCopy.setOnClickListener { listener.onCopyClick(clip) }
            binding.btnPin.setOnClickListener { listener.onPinClick(clip) }
            binding.btnFavorite.setOnClickListener { listener.onFavoriteClick(clip) }
            binding.btnShare.setOnClickListener { listener.onShareClick(clip) }
            binding.btnDelete.setOnClickListener { listener.onDeleteClick(clip) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<ClipListItem>() {
        override fun areItemsTheSame(oldItem: ClipListItem, newItem: ClipListItem): Boolean {
            return when {
                oldItem is ClipListItem.Header && newItem is ClipListItem.Header -> oldItem.title == newItem.title
                oldItem is ClipListItem.Clip && newItem is ClipListItem.Clip -> oldItem.clip.id == newItem.clip.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: ClipListItem, newItem: ClipListItem): Boolean {
            return oldItem == newItem
        }
    }
}
