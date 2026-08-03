package com.inscopelabs.abx.clipinbox.export.connector

import com.inscopelabs.abx.clipinbox.domain.detect.ClipType
import com.inscopelabs.abx.clipinbox.export.mime.CustomMimeTypes

/**
 * The wire envelope for a Tier 2 send of a single clip into the
 * abx-server mailbox.
 *
 * Feature 13 — Auto-Save + Batch Queue.
 */
data class MailboxSendRequest(
    val session: MailboxSession,
    val suggestedName: String,
    val type: ClipType,
    val content: String,
    val mime: String?,
    val sourceUri: String?,
    val mimeType: String = CustomMimeTypes.APP_MAILBOX_SEND_REQUEST,
) {
    init {
        require(suggestedName.isNotBlank()) { "name required" }
        require(content.isNotEmpty()) { "content required" }
    }
}
