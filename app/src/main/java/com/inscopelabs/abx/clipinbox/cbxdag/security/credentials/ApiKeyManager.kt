package com.inscopelabs.abx.clipinbox.cbxdag.security.credentials

/**
 * Securely stores the API key issued by CBX‑LINK for a delegation.
 *
 * POC PLACEHOLDER – In production, use EncryptedSharedPreferences
 * or Android Keystore. Never store a deterministic key.
 */
class ApiKeyManager {
    fun storeApiKey(delegationId: String, apiKey: String) {
        // TODO: Store securely (e.g., encrypted preferences).
        // POC: no‑op.
    }

    fun getApiKey(delegationId: String): String? {
        // TODO: Retrieve from secure storage.
        // POC PLACEHOLDER – always returns null to avoid accidental use.
        return null
    }

    fun clearApiKey(delegationId: String) {
        // TODO: Remove securely.
    }
}