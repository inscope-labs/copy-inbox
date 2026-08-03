package com.inscopelabs.abx.clipinbox.utils

import android.content.Context

object NotificationPreferences {
    private const val PREFS_NAME = "clipinbox_prefs"
    private const val KEY_PERSISTENT_NOTIFICATION = "persistent_notification_enabled"

    fun isPersistentNotificationEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PERSISTENT_NOTIFICATION, false)
    }

    fun setPersistentNotificationEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PERSISTENT_NOTIFICATION, enabled).apply()
    }
}
