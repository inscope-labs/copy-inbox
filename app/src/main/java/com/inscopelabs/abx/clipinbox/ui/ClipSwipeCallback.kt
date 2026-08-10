package com.inscopelabs.abx.clipinbox.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.domain.ClipRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ClipSwipeCallback(
    private val context: Context,
    private val scope: CoroutineScope,
    private val repository: ClipRepository,
    private val adapter: ClipListAdapter,
    private val getRootView: () -> View?
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

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
            scope.launch {
                repository.archiveClip(clip)
                Logger.i("ClipSwipeCallback", "Clip ${clip.id} archived via swipe right")
                val view = getRootView() ?: return@launch
                Snackbar.make(view, context.getString(R.string.home_toast_clip_archived), Snackbar.LENGTH_LONG)
                    .setAction(context.getString(R.string.home_action_undo)) {
                        scope.launch {
                            repository.updateClip(clip.copy(isArchived = false))
                            Logger.i("ClipSwipeCallback", "Clip ${clip.id} restored from archive")
                        }
                    }
                    .show()
            }
        } else if (direction == ItemTouchHelper.LEFT) {
            scope.launch {
                repository.deleteClip(clip)
                Logger.i("ClipSwipeCallback", "Clip ${clip.id} deleted via swipe left")
                val view = getRootView() ?: return@launch
                Snackbar.make(view, context.getString(R.string.home_toast_clip_deleted), Snackbar.LENGTH_LONG)
                    .setAction(context.getString(R.string.home_action_undo)) {
                        scope.launch {
                            repository.updateClip(clip)
                            Logger.i("ClipSwipeCallback", "Clip ${clip.id} restored from deletion")
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
        val archiveIcon = ContextCompat.getDrawable(context, R.drawable.ic_archive)
        val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete)

        if (dX > 0) {
            background.color = ContextCompat.getColor(context, R.color.pastel_green_archive)
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
            background.color = ContextCompat.getColor(context, R.color.pastel_red_delete)
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
