package com.inscopelabs.abx.clipinbox.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.ui.MainActivity
import com.inscopelabs.abx.clipinbox.ui.TransparentCaptureActivity

class NotificationHelper(
    private val context: Context,
    private val preferences: NotificationPreferences = NotificationPreferences(context)
) {
    fun buildOverlayNotification(): Notification {
        createNotificationChannel(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText("Overlay history service active")
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setOngoing(true)
            .build()
    }

    fun showOtp(code: String, capturedAt: Long) {
        createNotificationChannel(context)
        val copyIntent = Intent(context, TransparentCaptureActivity::class.java).apply {
            putExtra("otp_code", code)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            copyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("OTP Detected")
            .setContentText("Tap to copy code: $code")
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(OTP_NOTIFICATION_ID, notification)
            Logger.i("NotificationHelper", "Showed OTP notification for code")
        } catch (e: SecurityException) {
            Logger.w("NotificationHelper", "Permission for POST_NOTIFICATIONS not granted", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "clipinbox_trigger_channel"
        const val NOTIFICATION_ID = 1001
        const val OVERLAY_NOTIFICATION_ID = 2001
        const val OTP_NOTIFICATION_ID = 2002

        fun postTriggerNotification(
            context: Context,
            persistent: Boolean = NotificationPreferences.isPersistentNotificationEnabled(context)
        ) {
            val helper = NotificationHelper(context)
            helper.createNotificationChannel(context)

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
                context.getString(R.string.notification_action_capture),
                capturePendingIntent
            ).build()

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_content_text))
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
    }
}
