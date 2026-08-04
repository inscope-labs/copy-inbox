package com.inscopelabs.abx.clipinbox.ui

import android.view.Menu
import android.view.MenuInflater
import com.inscopelabs.abx.clipinbox.R

object DebugMenuInflater {
    fun inflate(inflater: MenuInflater, menu: Menu) {
        inflater.inflate(R.menu.main_toolbar_menu_debug, menu)
    }
}
