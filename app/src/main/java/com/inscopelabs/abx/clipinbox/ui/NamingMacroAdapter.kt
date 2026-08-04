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
import com.inscopelabs.abx.clipinbox.data.local.NamingMacro

class NamingMacroAdapter(
    private val listener: OnMacroClickListener
) : ListAdapter<NamingMacro, NamingMacroAdapter.ViewHolder>(DiffCallback) {

    interface OnMacroClickListener {
        fun onEditMacro(macro: NamingMacro)
        fun onDeleteMacro(macro: NamingMacro)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_naming_macro, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLabel: TextView = itemView.findViewById(R.id.tv_macro_label)
        private val tvTemplate: TextView = itemView.findViewById(R.id.tv_macro_template)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_macro)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_macro)

        fun bind(macro: NamingMacro) {
            tvLabel.text = macro.label
            tvTemplate.text = macro.template
            btnEdit.setOnClickListener {
                listener.onEditMacro(macro)
            }
            btnDelete.setOnClickListener {
                listener.onDeleteMacro(macro)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<NamingMacro>() {
        override fun areItemsTheSame(oldItem: NamingMacro, newItem: NamingMacro): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NamingMacro, newItem: NamingMacro): Boolean {
            return oldItem == newItem
        }
    }
}
