package com.inscopelabs.abx.clipinbox.service.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.utils.NotificationHelper
import com.inscopelabs.abx.clipinbox.utils.NotificationPreferences

/**
 * Foreground service that hosts the floating overlay.
 *
 * Feature 14 — Floating Clipboard History Overlay. The service is
 * `FOREGROUND_SERVICE_SPECIAL_USE` because the overlay is a user-driven
 * UI surface, not background work.
 */
class OverlayService : Service() {

    private lateinit var controller: OverlayWindowController
    private lateinit var gate: OverlayPermissionGate
    private lateinit var notifications: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        Logger.i("OverlayService", "onCreate")
        controller = OverlayWindowController(this)
        gate = OverlayPermissionGate(this)
        notifications = NotificationHelper(this, NotificationPreferences(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.i("OverlayService", "onStartCommand")
        if (!gate.canDrawOverlays()) {
            Logger.w("OverlayService", "Overlay permission not granted, stopping service")
            stopSelf()
            return START_NOT_STICKY
        }
        startInForegroundCompat()
        controller.show()
        return START_STICKY
    }

    override fun onDestroy() {
        Logger.i("OverlayService", "onDestroy")
        controller.hide()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startInForegroundCompat() {
        Logger.d("OverlayService", "startInForegroundCompat")
        val notification = notifications.buildOverlayNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NotificationHelper.OVERLAY_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NotificationHelper.OVERLAY_NOTIFICATION_ID, notification)
        }
    }

    companion object {
        fun start(context: Context) {
            Logger.i("OverlayService", "Starting service request")
            val intent = Intent(context, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            Logger.i("OverlayService", "Stopping service request")
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }
}
