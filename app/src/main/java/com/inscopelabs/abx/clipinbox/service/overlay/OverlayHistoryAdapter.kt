package com.inscopelabs.abx.clipinbox.service.overlay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.utils.TimeFormatter

/**
 * RecyclerView adapter for the floating overlay's history list.
 *
 * Feature 14 — Floating Clipboard History Overlay.
 */
class OverlayHistoryAdapter :
    RecyclerView.Adapter<OverlayHistoryAdapter.VH>() {

    data class Item(
        val id: Long,
        val preview: String,
        val capturedAt: Long,
    )

    private val items = mutableListOf<Item>()

    fun submit(newItems: List<Item>) {
        val diff = DiffUtil.calculateDiff(Diff(items, newItems))
        items.clear()
        items.addAll(newItems)
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_overlay_history, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val preview: TextView = itemView.findViewById(R.id.overlay_history_preview)
        private val timestamp: TextView = itemView.findViewById(R.id.overlay_history_time)

        fun bind(item: Item) {
            preview.text = item.preview
            timestamp.text = TimeFormatter.shortRelative(item.capturedAt)
        }
    }

    private class Diff(
        private val old: List<Item>,
        private val new: List<Item>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = old.size
        override fun getNewListSize(): Int = new.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition].id == new[newItemPosition].id
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition] == new[newItemPosition]
    }
}
