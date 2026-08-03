package com.inscopelabs.abx.clipinbox.export.qr

/**
 * QR code generation presets.
 *
 * Feature 1 — QR Generator. Each preset controls error correction level,
 * module size, and quiet zone to match typical scan distances.
 */
enum class QrPresetType(val displayName: String) {
    COMPACT("Compact"),
    STANDARD("Standard"),
    ROBUST("Robust — high error correction"),
    PRINT("Print — large modules"),
}
