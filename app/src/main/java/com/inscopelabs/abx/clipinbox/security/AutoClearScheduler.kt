package com.inscopelabs.abx.clipinbox.security

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.inscopelabs.abx.clipinbox.diagnostics.Logger

/**
 * Schedules a clipboard self-clear after a configurable delay.
 *
 * Feature 9 — Self-Clearing Clipboard. The scheduler only clears clips
 * it believes it placed (tracked by a marker token in [ClipData extras]),
 * so it won't trample on something the user copied from another app.
 */
class AutoClearScheduler(
    private val context: Context,
    private val delayMillis: Long = DEFAULT_DELAY_MS,
) {
    private val handler = Handler(Looper.getMainLooper())
    private val pending = Runnable { runClear() }

    private val clipboard: ClipboardManager by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    fun scheduleClear() {
        Logger.i("AutoClearScheduler", "Scheduling auto-clear in ${delayMillis}ms")
        handler.removeCallbacks(pending)
        handler.postDelayed(pending, delayMillis)
    }

    fun cancel() {
        Logger.i("AutoClearScheduler", "Cancelling pending auto-clear")
        handler.removeCallbacks(pending)
    }

    private fun runClear() {
        val current = clipboard.primaryClip
        if (current == null) {
            Logger.d("AutoClearScheduler", "Primary clip is null, skipping clear")
            return
        }
        if (!isMarkedByUs(current)) {
            Logger.d("AutoClearScheduler", "Primary clip not marked by us, skipping clear")
            return
        }
        Logger.i("AutoClearScheduler", "Executing clipboard self-clear")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            clipboard.clearPrimaryClip()
        } else {
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }

    private fun isMarkedByUs(clip: ClipData): Boolean {
        val token = MARKER_KEY?.let { clip.description.extras?.getString(it) } ?: return false
        return token == MARKER_VALUE
    }

    companion object {
        const val DEFAULT_DELAY_MS = 45_000L
        // The actual keys here are intentionally the platform extras so the
        // platform's own "sensitive" UI is also suppressed.
        const val MARKER_KEY = "android.content.extra.IS_SENSITIVE"
        const val MARKER_VALUE = "clipinbox"
    }
}
