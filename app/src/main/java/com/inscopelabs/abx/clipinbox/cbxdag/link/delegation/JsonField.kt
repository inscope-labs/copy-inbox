package com.inscopelabs.abx.clipinbox.cbxdag.link.delegation

/**
 * Minimal string-field extractor for small, known-shape JSON responses.
 *
 * Deliberately avoids org.json (an Android SDK class unreliable under a
 * plain JVM unit-test run without Robolectric) so callers stay testable
 * in CI without network or Android runtime dependencies. Not a general
 * JSON parser — only handles flat string-valued fields, which is all of
 * CBX-LINK's current response shapes need.
 */
object JsonField {
    fun extractString(json: String, field: String): String {
        return extractStringOrNull(json, field)
            ?: throw IllegalArgumentException("Field '$field' not found in response: $json")
    }

    fun extractStringOrNull(json: String, field: String): String? {
        val regex = Regex("\"${Regex.escape(field)}\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
        val match = regex.find(json) ?: return null
        return match.groupValues[1]
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }
}
