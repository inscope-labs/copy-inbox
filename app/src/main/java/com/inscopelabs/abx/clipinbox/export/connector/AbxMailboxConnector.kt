package com.inscopelabs.abx.clipinbox.export.connector

import com.inscopelabs.abx.clipinbox.diagnostics.Logger

class AbxMailboxConnector(
    private val gate: SessionGate,
) : FileManagerConnector {

    override fun send(request: MailboxSendRequest): Result<Unit> {
        val session = gate.currentSessionOrNull()
            ?: return Result.failure(
                IllegalStateException("no active abx-server session")
            )
        return try {
            Logger.i(TAG, "send: name=${request.suggestedName} " +
                "type=${request.type} mailbox=${session.mailboxId}")
            // Phase 5 stub: session is valid, log and return success.
            // Real HTTP/AIDL dispatch added in Phase 6 wiring.
            Logger.d(TAG, "send: stub — would POST to mailbox ${session.mailboxId}")
            Result.success(Unit)
        } catch (t: Throwable) {
            Logger.e(TAG, "send failed: ${t.message}")
            Result.failure(t)
        }
    }

    companion object {
        private const val TAG = "AbxMailboxConnector"
    }
}
