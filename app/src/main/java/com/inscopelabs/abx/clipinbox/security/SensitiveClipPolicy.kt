package com.inscopelabs.abx.clipinbox.security

import com.inscopelabs.abx.clipinbox.diagnostics.Logger

class SensitiveClipPolicy {
    fun isSensitive(text: String): Boolean {
        val sensitiveKeywords = listOf("password", "secret", "token", "private_key", "bearer")
        val isSens = sensitiveKeywords.any { text.lowercase().contains(it) }
        if (isSens) {
            Logger.w("SensitiveClipPolicy", "Detected sensitive clipboard content")
        }
        return isSens
    }
}
