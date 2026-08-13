package com.inscopelabs.abx.clipinbox.cbxdag.architecture

/**
 * Centralized POC limits and policies.
 * These are non‑negotiable for v0.1.
 */
object DagPolicy {
    const val INLINE_MAX_BYTES = 256 * 1024          // 256 KB
    const val ATTACHMENT_MAX_BYTES = 25 * 1024 * 1024 // 25 MB
    const val DELEGATION_MAX_BYTES = 50 * 1024 * 1024 // 50 MB
    const val DEFAULT_HEARTBEAT_INTERVAL_SECONDS = 300 // 5 minutes

    // Manifest version
    const val MANIFEST_VERSION = "0.1"
}