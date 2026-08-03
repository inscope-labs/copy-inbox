package com.inscopelabs.abx.clipinbox.security

import com.inscopelabs.abx.clipinbox.diagnostics.Logger

/**
 * Masks sensitive-looking substrings in a clip for display.
 *
 * Feature 8 — Sensitive Copy Hardening. Pairs with [SensitiveClipPolicy]:
 * the policy decides whether to redact, the redactor decides what the
 * user sees.
 */
class ClipRedactor(
    private val cardPattern: Regex = Regex("""\b(?:\d[ -]?){13,19}\b"""),
    private val ssnPattern: Regex = Regex("""\b\d{3}-\d{2}-\d{4}\b"""),
) {

    fun redact(content: String): String {
        Logger.d("ClipRedactor", "Redacting sensitive patterns in content len=${content.length}")
        var out = cardPattern.replace(content) { match ->
            maskDigits(match.value)
        }
        out = ssnPattern.replace(out) { match ->
            "•••-••-" + match.value.takeLast(4)
        }
        return out
    }

    private fun maskDigits(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        if (digits.length <= LAST4) return "•".repeat(digits.length)
        val tail = digits.takeLast(LAST4)
        return "•".repeat(digits.length - LAST4) + " " + tail
    }

    companion object {
        private const val LAST4 = 4
    }
}
