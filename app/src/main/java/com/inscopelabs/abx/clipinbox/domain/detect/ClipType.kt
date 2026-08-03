package com.inscopelabs.abx.clipinbox.domain.detect

/**
 * Coarse classification of a clipboard payload.
 *
 * Used by the reactive pipeline (Feature 4) to route clips into the right
 * downstream handler (URI handoff, MIME exchange, OTP capture, plain text).
 */
enum class ClipType {
    TEXT,
    URL,
    EMAIL,
    PHONE,
    OTP,
    IMAGE_REFERENCE,
    URI,
    FILE_PATH,
    SENSITIVE,
    UNKNOWN;

    val isShareable: Boolean
        get() = this != SENSITIVE && this != UNKNOWN
}
