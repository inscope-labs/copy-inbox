package com.inscopelabs.abx.clipinbox.utils

import android.content.ClipDescription
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import java.util.concurrent.atomic.AtomicLong

/**
 * Immutable record of a clipboard read at a specific moment in time.
 *
 * Feature 10 — TOCTOU-Safe Read Handling. The point of a snapshot is to
 * freeze "what we saw" so downstream code (redaction, classification,
 * OTP capture) can't be tricked by a concurrent clipboard change.
 */
data class ClipSnapshot(
    val id: Long,
    val text: String,
    val mime: String,
    val capturedAt: Long,
    val extras: Map<String, String>,
) {
    companion object {
        private val idGen = AtomicLong(0L)

        fun capture(text: String, description: ClipDescription?): ClipSnapshot {
            val extras = mutableMapOf<String, String>()
            description?.extras?.let { bundle ->
                for (key in bundle.keySet()) {
                    val v = bundle.get(key) ?: continue
                    extras[key] = v.toString()
                }
            }
            val id = idGen.incrementAndGet()
            Logger.d("ClipSnapshot", "Captured snapshot #$id")
            return ClipSnapshot(
                id = id,
                text = text,
                mime = description?.getMimeType(0) ?: "text/plain",
                capturedAt = System.nanoTime(),
                extras = extras,
            )
        }
    }
}
