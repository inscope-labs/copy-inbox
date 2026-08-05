package com.inscopelabs.abx.clipinbox.category

import android.content.Context

class CategoryPreferences(private val context: Context) {

    fun isSaveDialogEnabled(): Boolean = isSaveDialogEnabled(context)

    companion object {
        private const val PREFS_NAME = "clipinbox_prefs"
        private const val KEY_SAVE_DIALOG_ENABLED = "category_save_dialog_enabled"

        fun isSaveDialogEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_SAVE_DIALOG_ENABLED, false)
        }

        fun setSaveDialogEnabled(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_SAVE_DIALOG_ENABLED, enabled).apply()
        }
    }
}
