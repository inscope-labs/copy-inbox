package com.inscopelabs.abx.clipinbox.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.data.local.CategoryEntity

class CategoryAdapter(
    private val listener: OnCategoryClickListener
) : ListAdapter<CategoryEntity, CategoryAdapter.ViewHolder>(DiffCallback) {

    interface OnCategoryClickListener {
        fun onSetDefault(category: CategoryEntity)
        fun onDeleteCategory(category: CategoryEntity)
        fun onEditCategory(category: CategoryEntity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_category_name)
        private val tvDefaultBadge: TextView = itemView.findViewById(R.id.tv_category_default_badge)
        private val viewColor: View = itemView.findViewById(R.id.view_category_color)
        private val btnSetDefault: ImageButton = itemView.findViewById(R.id.btn_set_default)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_category)

        fun bind(category: CategoryEntity) {
            tvName.text = category.name
            tvDefaultBadge.isVisible = category.isDefault

            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                try {
                    setColor(Color.parseColor(category.colorHex))
                } catch (e: Exception) {
                    setColor(Color.parseColor("#5B6EE8"))
                }
            }
            viewColor.background = drawable

            btnSetDefault.setOnClickListener {
                listener.onSetDefault(category)
            }

            btnDelete.setOnClickListener {
                listener.onDeleteCategory(category)
            }

            itemView.setOnClickListener {
                listener.onEditCategory(category)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CategoryEntity>() {
        override fun areItemsTheSame(oldItem: CategoryEntity, newItem: CategoryEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CategoryEntity, newItem: CategoryEntity): Boolean {
            return oldItem == newItem
        }
    }
}
