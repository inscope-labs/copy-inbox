package com.inscopelabs.abx.clipinbox.ui

import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.diagnostics.DebugToolsLauncher

object DebugMenuHandler {
    fun handle(item: MenuItem, activity: FragmentActivity): Boolean {
        return when (item.itemId) {
            R.id.action_view_logs -> {
                DebugToolsLauncher.showLogViewer(activity)
                true
            }
            else -> false
        }
    }
}
