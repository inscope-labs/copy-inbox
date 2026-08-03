package com.inscopelabs.abx.clipinbox.domain.naming

import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.domain.detect.ClipType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates human-friendly default file/queue names for a clip.
 *
 * Feature 13 — Auto-Save + Batch Queue.
 *
 * Names are deterministic per (type, timestampBucket) so re-running the
 * namer for the same clip yields the same label — useful for the queue UI
 * which diffs incoming items against existing entries.
 */
class ClipAutoNamer(
    private val timestampFormatter: SimpleDateFormat =
        SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US),
) {

    fun suggest(type: ClipType, content: String, now: Date = Date()): String {
        val stamp = timestampFormatter.format(now)
        val slug = slugify(content, MAX_SLUG)
        val prefix = prefixFor(type)
        val name = if (slug.isEmpty()) "$prefix-$stamp" else "$prefix-$stamp-$slug"
        Logger.d("ClipAutoNamer", "Suggested name '$name' for clip type $type")
        return name
    }

    private fun prefixFor(type: ClipType): String = when (type) {
        ClipType.TEXT -> "note"
        ClipType.URL -> "link"
        ClipType.EMAIL -> "email"
        ClipType.PHONE -> "phone"
        ClipType.OTP -> "otp"
        ClipType.URI -> "uri"
        ClipType.FILE_PATH -> "file"
        ClipType.IMAGE_REFERENCE -> "image"
        ClipType.SENSITIVE -> "secret"
        ClipType.UNKNOWN -> "clip"
    }

    private fun slugify(input: String, maxLen: Int): String {
        val builder = StringBuilder(maxLen)
        for (ch in input) {
            if (builder.length >= maxLen) break
            val normalized = when {
                ch.isLetterOrDigit() -> ch.lowercaseChar()
                ch == ' ' || ch == '-' || ch == '_' -> '-'
                else -> '-'
            }
            if (normalized == '-' && (builder.isEmpty() || builder.last() == '-')) continue
            builder.append(normalized)
        }
        return builder.toString().trim('-')
    }

    companion object {
        private const val MAX_SLUG = 24
    }
}
