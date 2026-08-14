package com.inscopelabs.abx.clipinbox.cbxdag.data.provider

import java.nio.charset.StandardCharsets

/**
 * Generates a deterministic placeholder byte payload for a resource.
 *
 * The POC resource providers don't yet read real content (clipboard/SAF
 * access is Android-integration work for a later phase). Until then, this
 * gives each node a real, reproducible SHA-256 hash instead of a hardcoded
 * dummy string — the hash is genuine, the underlying content is not.
 *
 * Replace call sites with real content bytes once each provider reads
 * actual resource data.
 */
object PlaceholderContent {
    fun bytesFor(resourceId: String, type: String): ByteArray {
        return "cbxdag-poc-placeholder:$type:$resourceId".toByteArray(StandardCharsets.UTF_8)
    }
}
