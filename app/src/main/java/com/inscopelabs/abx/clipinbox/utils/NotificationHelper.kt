package com.inscopelabs.abx.clipinbox.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.inscopelabs.abx.clipinbox.ui.MainActivity
import com.inscopelabs.abx.clipinbox.ui.TransparentCaptureActivity

object NotificationHelper {
    const val CHANNEL_ID = "clipinbox_trigger_channel"
    const val NOTIFICATION_ID = 1001

    fun postTriggerNotification(
        context: Context,
        persistent: Boolean = NotificationPreferences.isPersistentNotificationEnabled(context)
    ) {
        createNotificationChannel(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val captureIntent = Intent(context, TransparentCaptureActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val capturePendingIntent = PendingIntent.getActivity(
            context,
            1,
            captureIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val captureAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_save,
            "Capture Clipboard",
            capturePendingIntent
        ).build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("ClipInBox")
            .setContentText("Quick clipboard capture ready")
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentIntent(contentPendingIntent)
            .addAction(captureAction)
            .setOngoing(persistent)
            .setAutoCancel(!persistent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w("NotificationHelper", "Permission for POST_NOTIFICATIONS might not be granted", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ClipInBox Shortcut",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Dismissible notification shortcut to quickly capture clipboard text"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }
}
