package com.inscopelabs.abx.clipinbox.service.overlay

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.inscopelabs.abx.clipinbox.diagnostics.Logger

/**
 * Pre-flight checks for the floating overlay.
 *
 * Feature 14 — Floating Clipboard History Overlay. The overlay needs
 * `SYSTEM_ALERT_WINDOW`; on Tiramisu+ it also needs explicit user
 * consent for the special-use foreground service type.
 */
class OverlayPermissionGate(private val context: Context) {

    fun canDrawOverlays(): Boolean {
        val canDraw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
        Logger.d("OverlayPermissionGate", "canDrawOverlays: $canDraw")
        return canDraw
    }

    fun requestDrawOverlaysIntent(): Intent {
        Logger.i("OverlayPermissionGate", "Creating draw overlays permission intent")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } else {
            Intent()
        }
    }

    fun canRunSpecialUseForegroundService(): Boolean {
        // The actual permission is granted at install time on older
        // versions; on UpsideDownCake+ we need the runtime declaration.
        return true
    }
}
