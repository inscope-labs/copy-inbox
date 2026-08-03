package com.inscopelabs.abx.clipinbox.export.connector

/**
 * Sends a [MailboxSendRequest] into the abx-server mailbox.
 *
 * Feature 13 — Tier 2 send. The connector is intentionally a single
 * function: it either succeeds (and the queue marks the row sent) or it
 * raises — the queue manager decides retry policy.
 */
fun interface FileManagerConnector {
    fun send(request: MailboxSendRequest): Result<Unit>
}
