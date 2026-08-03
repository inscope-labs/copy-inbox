package com.inscopelabs.abx.clipinbox.export.mime

import com.inscopelabs.abx.clipinbox.diagnostics.Logger

/**
 * Central registry of MIME types used by the ClipInBox ↔ abx-server mailbox
 * exchange protocol.
 *
 * Feature 3 — Custom MIME Exchange.
 *
 * Anything that crosses the IPC boundary — both directions — should be
 * described by a value here. Plain text falls back to [TEXT_PLAIN] only
 * when no better type is known.
 */
object CustomMimeTypes {
    const val TEXT_PLAIN = "text/plain"
    const val TEXT_URI = "text/uri-list"
    const val TEXT_OTP = "application/vnd.abx.otp"
    const val TEXT_CLIP_SNAPSHOT = "application/vnd.abx.clip-snapshot"
    const val APP_CLIP_QUEUE_ITEM = "application/vnd.abx.clip-queue-item"
    const val APP_MAILBOX_SEND_REQUEST = "application/vnd.abx.mailbox-send-request"
    const val APP_MAILBOX_SEND_RESULT = "application/vnd.abx.mailbox-send-result"
    const val APP_ABX_SESSION = "application/vnd.abx.session"

    fun isCustomAbxType(mime: String?): Boolean {
        val result = mime != null && mime.startsWith("application/vnd.abx.")
        Logger.d("CustomMimeTypes", "isCustomAbxType for $mime: $result")
        return result
    }
}
