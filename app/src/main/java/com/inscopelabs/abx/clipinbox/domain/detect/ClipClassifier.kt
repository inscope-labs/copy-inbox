package com.inscopelabs.abx.clipinbox.domain.detect

import android.util.Patterns
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.utils.HashGenerator

/**
 * Heuristic classifier for raw clipboard strings.
 *
 * Feature 4 — Smart Clip Reactive Pipeline.
 *
 * Classification order matters: more specific patterns (OTP, phone) win over
 * the generic URL/email detectors. Sensitive detection is a separate concern
 * and runs in [com.inscopelabs.abx.clipinbox.security.SensitiveClipPolicy].
 */
class ClipClassifier(
    private val otpPattern: Regex = DEFAULT_OTP,
    private val phonePattern: Regex = DEFAULT_PHONE,
) {

    fun classify(raw: String): ClipType {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            Logger.d("ClipClassifier", "Classified empty string as UNKNOWN")
            return ClipType.UNKNOWN
        }

        // OTP must be checked first — 4–8 digit codes can look like phone fragments.
        if (otpPattern.matches(trimmed)) {
            Logger.i("ClipClassifier", "Classified clip as OTP")
            return ClipType.OTP
        }

        val type = when {
            trimmed.startsWith("content://", ignoreCase = true) -> ClipType.URI
            trimmed.startsWith("file://", ignoreCase = true) -> ClipType.FILE_PATH
            trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) -> ClipType.URL
            Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() -> ClipType.EMAIL
            phonePattern.matches(trimmed) -> ClipType.PHONE
            Patterns.WEB_URL.matcher(trimmed).matches() -> ClipType.URL
            trimmed.length < MIN_TEXT_LENGTH -> ClipType.UNKNOWN
            else -> ClipType.TEXT
        }

        Logger.i("ClipClassifier", "Classified clip len=${trimmed.length} as $type")
        return type
    }

    /**
     * Convenience: produce a stable signature for a clip used by the
     * reactive pipeline to deduplicate near-identical copies.
     */
    fun signature(raw: String): String {
        val sig = HashGenerator.sha1(raw.trim().lowercase())
        Logger.d("ClipClassifier", "Generated signature for clip")
        return sig
    }

    companion object {
        private const val MIN_TEXT_LENGTH = 2

        // 4–8 digit code, optionally surrounded by whitespace; no word boundaries
        // so we don't accidentally swallow phone numbers.
        private val DEFAULT_OTP = Regex("""^\d{4,8}$""")
        private val DEFAULT_PHONE = Regex("""^\+?[0-9\-\s().]{7,20}$""")
    }
}
