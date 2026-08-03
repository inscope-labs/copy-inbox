package com.inscopelabs.abx.clipinbox.service

import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.utils.ClipSnapshot
import com.inscopelabs.abx.clipinbox.utils.NotificationHelper

/**
 * Auto-captures OTP codes from the clipboard and surfaces them via a
 * dismissable notification.
 *
 * Feature 5 — OTP Auto-Capture. The capture only fires for codes the
 * [com.inscopelabs.abx.clipinbox.domain.detect.ClipClassifier] marked as
 * [com.inscopelabs.abx.clipinbox.domain.detect.ClipType.OTP], and is
 * rate-limited so a notification storm is not possible.
 */
class OtpAutoCapture(
    private val context: Context,
    private val notifications: NotificationHelper,
    private val cooldownMs: Long = DEFAULT_COOLDOWN_MS,
) {
    private val clipboard: ClipboardManager by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private val manager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Volatile private var lastFiredAt: Long = 0L

    fun maybeCapture(code: String, snapshot: ClipSnapshot) {
        val now = System.currentTimeMillis()
        if (now - lastFiredAt < cooldownMs) {
            Logger.d("OtpAutoCapture", "OTP capture in cooldown phase, ignoring")
            return
        }
        lastFiredAt = now
        Logger.i("OtpAutoCapture", "Capturing OTP code")
        notifications.showOtp(code, snapshot.capturedAt)
    }

    /**
     * Convenience: if a foreground app has the OTP field focused, this
     * pastes via the platform clipboard. Returns true on success.
     */
    fun copyToClipboard(code: String): Boolean {
        Logger.i("OtpAutoCapture", "Copying OTP code to clipboard")
        val clip = ClipData.newPlainText("OTP", code)
        clipboard.setPrimaryClip(clip)
        manager.cancel(NotificationHelper.OTP_NOTIFICATION_ID)
        return true
    }

    companion object {
        const val DEFAULT_COOLDOWN_MS = 20_000L
    }
}
