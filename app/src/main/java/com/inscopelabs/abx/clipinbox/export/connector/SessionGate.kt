package com.inscopelabs.abx.clipinbox.export.connector

import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.export.mime.CustomMimeTypes

/**
 * Validated abx-server session envelope.
 *
 * Feature 13 — Auto-Save + Batch Queue. The gate wraps whatever
 * authentication context abx-server hands us, and refuses to issue a
 * session for unauthenticated callers.
 */
class SessionGate(
    private val sessionStore: SessionStore,
) {

    fun currentSessionOrNull(): MailboxSession? {
        val raw = sessionStore.read() ?: run {
            Logger.d("SessionGate", "No session found in store")
            return null
        }
        if (raw.token.isBlank() || raw.expiresAt <= System.currentTimeMillis()) {
            Logger.w("SessionGate", "Session token blank or expired, clearing store")
            sessionStore.clear()
            return null
        }
        Logger.d("SessionGate", "Valid session returned for mailbox ${raw.mailboxId}")
        return raw
    }

    fun requireSession(): MailboxSession =
        currentSessionOrNull() ?: error("no active abx-server session")

    fun accept(raw: RawSession) {
        require(raw.token.isNotBlank()) { "empty session token" }
        require(raw.expiresAt > System.currentTimeMillis()) { "session already expired" }
        Logger.i("SessionGate", "Accepting new session for mailbox ${raw.mailboxId}")
        sessionStore.write(MailboxSession(raw.token, raw.mailboxId, raw.expiresAt))
    }

    fun revoke() {
        Logger.i("SessionGate", "Revoking active session")
        sessionStore.clear()
    }

    interface SessionStore {
        fun read(): MailboxSession?
        fun write(session: MailboxSession)
        fun clear()
    }
}

data class MailboxSession(
    val token: String,
    val mailboxId: String,
    val expiresAt: Long,
    val mime: String = CustomMimeTypes.APP_ABX_SESSION,
)

/** Wire-level session payload before the gate validates it. */
data class RawSession(
    val token: String,
    val mailboxId: String,
    val expiresAt: Long,
)
