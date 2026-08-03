package com.inscopelabs.abx.clipinbox

import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.os.Bundle
import com.inscopelabs.abx.clipinbox.boot.BootGuard
import com.inscopelabs.abx.clipinbox.boot.BootRoute
import com.inscopelabs.abx.clipinbox.boot.RecoveryActivity
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

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity !is RecoveryActivity) {
                    BootRoute.redirectIfNeeded(activity)
                }
            }
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })

        BootGuard.stageStart("app_init")
        try {
            val database = ClipboardDatabase.getDatabase(this)
            repository = ClipRepositoryImpl(database.clipDao())

            if (NotificationPreferences.isPersistentNotificationEnabled(this)) {
                NotificationHelper.postTriggerNotification(this, true)
            }
            BootGuard.stageSuccess("app_init")
        } catch (t: Throwable) {
            BootGuard.recordFailure(this, "app_init", t)
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
