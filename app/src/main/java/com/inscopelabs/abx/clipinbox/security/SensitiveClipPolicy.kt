package com.inscopelabs.abx.clipinbox.security

import com.inscopelabs.abx.clipinbox.diagnostics.Logger

/**
 * Decides whether a clip is "sensitive" and must not leave the device
 * via any of the export paths.
 *
 * Feature 8 — Sensitive Copy Hardening. This is a heuristic, not a
 * cryptographic guarantee. It is the first line of defense; the [ClipRedactor]
 * masks sensitive-looking content before it is displayed.
 */
class SensitiveClipPolicy(
    private val keywords: Set<String> = DEFAULT_KEYWORDS,
    private val cardPattern: Regex = DEFAULT_CARD,
    private val ssnPattern: Regex = DEFAULT_SSN,
) {

    fun isSensitive(content: String): Boolean {
        val lower = content.lowercase()
        val sensitive = keywords.any { lower.contains(it) } ||
            cardPattern.containsMatchIn(content) ||
            ssnPattern.containsMatchIn(content)
        if (sensitive) {
            Logger.w("SensitiveClipPolicy", "Content flagged as sensitive (reason=${reason(content)})")
        } else {
            Logger.d("SensitiveClipPolicy", "Content check passed non-sensitive")
        }
        return sensitive
    }

    fun reason(content: String): String? = when {
        cardPattern.containsMatchIn(content) -> "card-number"
        ssnPattern.containsMatchIn(content) -> "ssn-shape"
        keywords.any { content.lowercase().contains(it) } -> "keyword"
        else -> null
    }

    companion object {
        private val DEFAULT_KEYWORDS = setOf(
            "password",
            "passwd",
            "secret",
            "api_key",
            "apikey",
            "private key",
            "bearer ",
            "access_token",
            "refresh_token",
        )

        // Loose Luhn-not-enforced match: 13–19 digit groupings. The redactor
        // takes care of formatting; we only decide yes/no here.
        private val DEFAULT_CARD = Regex("""\b(?:\d[ -]?){13,19}\b""")
        private val DEFAULT_SSN = Regex("""\b\d{3}-\d{2}-\d{4}\b""")
    }
}
