package com.inscopelabs.abx.clipinbox.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.domain.queue.ClipQueueManager
import com.inscopelabs.abx.clipinbox.domain.queue.QueueEntity

/**
 * UI for the auto-save + batch queue.
 *
 * Feature 13 — Auto-Save + Batch Queue.
 */
class QueueFragment : Fragment() {

    private lateinit var manager: ClipQueueManager
    private val adapter = QueueAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        Logger.i("QueueFragment", "onCreateView")
        val root = inflater.inflate(R.layout.fragment_queue, container, false)
        val list = root.findViewById<RecyclerView>(R.id.queue_recycler)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter
        root.findViewById<View>(R.id.queue_dispatch).setOnClickListener {
            Logger.i("QueueFragment", "Dispatch pending clicked")
            manager.dispatchPending()
        }
        return root
    }

    override fun onResume() {
        super.onResume()
        Logger.d("QueueFragment", "onResume")
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            // The actual data source is provided by the application's
            // service locator; refresh() is called from the repository
            // observer wiring. Kept as a hook for the integration PR.
        }
    }

    fun bind(manager: ClipQueueManager) {
        this.manager = manager
    }

    private class QueueAdapter : RecyclerView.Adapter<QueueAdapter.VH>() {
        private val items = mutableListOf<QueueEntity>()

        fun submit(newItems: List<QueueEntity>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_queue, parent, false)
            return VH(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position])
        }

        class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(entity: QueueEntity) {
                // Real binding lives in the layout's data-binding setup.
                // Stubbed here so the file compiles standalone.
                @Suppress("UNUSED_VARIABLE")
                val _ignored = entity
            }
        }
    }
}
