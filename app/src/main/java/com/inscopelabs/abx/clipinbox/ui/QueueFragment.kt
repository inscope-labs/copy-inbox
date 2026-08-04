package com.inscopelabs.abx.clipinbox.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inscopelabs.abx.clipinbox.ClipInBoxApplication
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.domain.queue.ClipQueueManager
import com.inscopelabs.abx.clipinbox.domain.queue.QueueEntity
import com.inscopelabs.abx.clipinbox.domain.queue.QueueRepositoryImpl
import kotlinx.coroutines.launch

/**
 * UI for the auto-save + batch queue.
 *
 * Feature 13 — Auto-Save + Batch Queue.
 */
class QueueFragment : Fragment() {

    private var manager: ClipQueueManager? = null
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
            val currentManager = manager
            if (currentManager != null) {
                currentManager.dispatchPending()
            } else {
                Logger.w("QueueFragment", "ClipQueueManager is not yet bound")
            }
        }
        return root
    }

    override fun onResume() {
        super.onResume()
        Logger.d("QueueFragment", "onResume")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val app = activity?.application as? ClipInBoxApplication
                val repo = app?.queueRepository as? QueueRepositoryImpl
                if (repo != null) {
                    Logger.d("QueueFragment", "Observing queue entities")
                    repo.observeAll().collect { items ->
                        Logger.d("QueueFragment", "Collected ${items.size} queue items")
                        adapter.submit(items)
                    }
                } else {
                    Logger.w("QueueFragment", "queueRepository is unavailable on Application")
                }
            }
        }
    }

    fun bind(manager: ClipQueueManager) {
        Logger.i("QueueFragment", "bind ClipQueueManager")
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
            private val nameText: TextView? = itemView.findViewById(R.id.queue_item_name)
            private val stateText: TextView? = itemView.findViewById(R.id.queue_item_state)

            fun bind(entity: QueueEntity) {
                nameText?.text = entity.suggestedName
                stateText?.text = entity.state.name
            }
        }
    }
}
