package com.inscopelabs.abx.clipinbox

import android.app.Application
import android.app.NotificationManager
import com.inscopelabs.abx.clipinbox.data.local.ClipboardDatabase
import com.inscopelabs.abx.clipinbox.domain.ClipRepository
import com.inscopelabs.abx.clipinbox.domain.ClipRepositoryImpl
import com.inscopelabs.abx.clipinbox.utils.NotificationHelper
import com.inscopelabs.abx.clipinbox.utils.NotificationPreferences

class ClipInBoxApplication : Application() {

    lateinit var repository: ClipRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = ClipboardDatabase.getDatabase(this)
        repository = ClipRepositoryImpl(database.clipDao())

        if (NotificationPreferences.isPersistentNotificationEnabled(this)) {
            NotificationHelper.postTriggerNotification(this, true)
        }
    }

    fun setNotificationTriggerEnabled(enabled: Boolean) {
        NotificationPreferences.setPersistentNotificationEnabled(this, enabled)
        if (enabled) {
            NotificationHelper.postTriggerNotification(this, true)
        } else {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(NotificationHelper.NOTIFICATION_ID)
        }
    }
}
