package com.inscopelabs.abx.clipinbox.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.data.local.TagEntity
import com.inscopelabs.abx.clipinbox.diagnostics.Logger

class TagAdapter(
    private val listener: OnTagClickListener
) : ListAdapter<TagEntity, TagAdapter.ViewHolder>(DiffCallback) {

    interface OnTagClickListener {
        fun onEditTag(tag: TagEntity)
        fun onDeleteTag(tag: TagEntity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tag, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLabel: TextView = itemView.findViewById(R.id.tv_tag_label)
        private val viewColor: View = itemView.findViewById(R.id.view_tag_color)
        private val ivLockIcon: ImageView = itemView.findViewById(R.id.iv_lock_icon)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_tag)

        fun bind(tag: TagEntity) {
            tvLabel.text = tag.label

            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                try {
                    setColor(Color.parseColor(tag.colorHex))
                } catch (e: Exception) {
                    setColor(Color.parseColor("#5B6EE8"))
                }
            }
            viewColor.background = drawable

            if (tag.isSystemReserved) {
                ivLockIcon.isVisible = true
                btnDelete.isVisible = false
                btnDelete.setOnClickListener(null)
                itemView.setOnClickListener(null)
            } else {
                ivLockIcon.isVisible = false
                btnDelete.isVisible = true
                btnDelete.setOnClickListener {
                    Logger.d(TAG, "Delete clicked for tag id=${tag.id}, label='${tag.label}'")
                    listener.onDeleteTag(tag)
                }
                itemView.setOnClickListener {
                    Logger.d(TAG, "Edit clicked for tag id=${tag.id}, label='${tag.label}'")
                    listener.onEditTag(tag)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TagEntity>() {
        private const val TAG = "TagAdapter"

        override fun areItemsTheSame(oldItem: TagEntity, newItem: TagEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TagEntity, newItem: TagEntity): Boolean {
            return oldItem == newItem
        }
    }
}
