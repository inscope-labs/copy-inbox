package com.inscopelabs.abx.clipinbox.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.data.local.SafPath

class SafPathAdapter(
    private val listener: OnPathClickListener
) : ListAdapter<SafPath, SafPathAdapter.ViewHolder>(DiffCallback) {

    interface OnPathClickListener {
        fun onDeletePath(path: SafPath)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saf_path, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLabel: TextView = itemView.findViewById(R.id.tv_path_label)
        private val tvUri: TextView = itemView.findViewById(R.id.tv_path_uri)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_path)

        fun bind(path: SafPath) {
            tvLabel.text = path.label
            tvUri.text = path.treeUri
            btnDelete.setOnClickListener {
                listener.onDeletePath(path)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SafPath>() {
        override fun areItemsTheSame(oldItem: SafPath, newItem: SafPath): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SafPath, newItem: SafPath): Boolean {
            return oldItem == newItem
        }
    }
}
