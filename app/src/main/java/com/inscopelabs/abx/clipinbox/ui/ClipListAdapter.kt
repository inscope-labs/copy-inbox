package com.inscopelabs.abx.clipinbox.ui

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.databinding.ItemClipBinding
import com.inscopelabs.abx.clipinbox.utils.TimeFormatter

class ClipListAdapter(
    private val listener: Listener
) : ListAdapter<ClipEntity, ClipListAdapter.ClipViewHolder>(DiffCallback) {

    private val selectedIds = mutableSetOf<Long>()

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

    fun getSelectedIds(): Set<Long> = selectedIds.toSet()

    fun getSelectedClips(): List<ClipEntity> {
        return currentList.filter { selectedIds.contains(it.id) }
    }

    fun clearSelection() {
        if (selectedIds.isNotEmpty()) {
            selectedIds.clear()
            notifyDataSetChanged()
            listener.onSelectionChanged(0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClipViewHolder {
        val binding = ItemClipBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ClipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ClipViewHolder(
        private val binding: ItemClipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(clip: ClipEntity) {
            val isSelected = selectedIds.contains(clip.id)

            binding.ivSelectionCheck.isVisible = isSelected
            binding.cardClip.strokeWidth = if (isSelected) 6 else 0

            binding.chipCategory.text = clip.category
            binding.tvTime.text = TimeFormatter.formatRelativeTime(clip.timestamp)
            binding.tvContent.text = clip.content

            if (clip.category == "Code") {
                binding.tvContent.typeface = Typeface.MONOSPACE
            } else {
                binding.tvContent.typeface = Typeface.DEFAULT
            }

            binding.tvMetaCounts.text = binding.root.context.getString(
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

    object DiffCallback : DiffUtil.ItemCallback<ClipEntity>() {
        override fun areItemsTheSame(oldItem: ClipEntity, newItem: ClipEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ClipEntity, newItem: ClipEntity): Boolean {
            return oldItem == newItem
        }
    }
}
