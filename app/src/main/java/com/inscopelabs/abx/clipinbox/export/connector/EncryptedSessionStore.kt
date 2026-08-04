package com.inscopelabs.abx.clipinbox.export.connector

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.inscopelabs.abx.clipinbox.diagnostics.Logger

class EncryptedSessionStore(context: Context) : SessionGate.SessionStore {

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context.applicationContext,
            "clip_session_store",
            MasterKey.Builder(context.applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun read(): MailboxSession? {
        val token     = prefs.getString(KEY_TOKEN, null)     ?: return null
        val mailboxId = prefs.getString(KEY_MAILBOX, null)   ?: return null
        val expiresAt = prefs.getLong(KEY_EXPIRES, 0L)
        Logger.d(TAG, "read: mailboxId=$mailboxId expiresAt=$expiresAt")
        return MailboxSession(token, mailboxId, expiresAt)
    }

    override fun write(session: MailboxSession) {
        Logger.i(TAG, "write: mailboxId=${session.mailboxId}")
        prefs.edit()
            .putString(KEY_TOKEN,    session.token)
            .putString(KEY_MAILBOX,  session.mailboxId)
            .putLong(KEY_EXPIRES,    session.expiresAt)
            .apply()
    }

    override fun clear() {
        Logger.i(TAG, "clear")
        prefs.edit().clear().apply()
    }

    companion object {
        private const val TAG        = "EncryptedSessionStore"
        private const val KEY_TOKEN   = "session_token"
        private const val KEY_MAILBOX = "mailbox_id"
        private const val KEY_EXPIRES = "expires_at"
    }
}
