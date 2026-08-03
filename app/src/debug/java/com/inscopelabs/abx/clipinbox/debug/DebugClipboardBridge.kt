package com.inscopelabs.abx.clipinbox.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.utils.ClipboardHelper

/**
 * Test-only entry points for poking the clipboard from instrumentation or
 * debug builds.
 *
 * Feature 12 — Debug Clipboard Bridge. This file lives in the `debug`
 * source set so it is never shipped in release builds.
 */
class DebugClipboardBridge(private val context: Context) {

    private val clipboard: ClipboardManager by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    fun pushText(text: String, label: String = "debug") {
        Logger.i("DebugClipboardBridge", "pushText called with label: $label")
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    fun readText(): String? {
        Logger.d("DebugClipboardBridge", "readText requested")
        return ClipboardHelper.read(context)
    }

    fun simulateOtpCopy(): String {
        val otp = (100000..999999).random().toString()
        Logger.i("DebugClipboardBridge", "Simulating OTP copy: $otp")
        pushText(otp, label = "otp-sim")
        return otp
    }

    fun clearClipboard() {
        Logger.i("DebugClipboardBridge", "Clearing clipboard")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            clipboard.clearPrimaryClip()
        } else {
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }
}
