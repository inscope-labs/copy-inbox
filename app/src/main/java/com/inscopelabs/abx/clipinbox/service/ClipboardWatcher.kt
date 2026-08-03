package com.inscopelabs.abx.clipinbox.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.domain.detect.ClipClassifier
import com.inscopelabs.abx.clipinbox.domain.detect.ClipType
import com.inscopelabs.abx.clipinbox.security.SensitiveClipPolicy
import com.inscopelabs.abx.clipinbox.utils.ClipSnapshot
import com.inscopelabs.abx.clipinbox.utils.ClipboardHelper
import java.util.concurrent.atomic.AtomicReference

/**
 * Wraps the platform's primary-clip listener and pushes a TOCTOU-safe
 * snapshot to subscribers.
 *
 * Features 4 + 5. Replaces the inline clipboard code that previously lived
 * in the existing helper. The watcher is the single source of truth for
 * "what was just copied" — every other component (overlay, OTP capture,
 * reactive pipeline) reads from [latest].
 */
class ClipboardWatcher(
    private val context: Context,
    private val classifier: ClipClassifier,
    private val policy: SensitiveClipPolicy,
    private val otpCapture: OtpAutoCapture,
) : ClipboardManager.OnPrimaryClipChangedListener {

    private val clipboard: ClipboardManager by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private val latest = AtomicReference<ClipSnapshot?>(null)
    private var installed = false

    fun install() {
        if (installed) return
        Logger.i("ClipboardWatcher", "Installing primary clip changed listener")
        clipboard.addPrimaryClipChangedListener(this)
        installed = true
    }

    fun uninstall() {
        if (!installed) return
        Logger.i("ClipboardWatcher", "Uninstalling primary clip changed listener")
        clipboard.removePrimaryClipChangedListener(this)
        installed = false
    }

    fun currentSnapshot(): ClipSnapshot? = latest.get()

    override fun onPrimaryClipChanged() {
        val data = clipboard.primaryClip ?: return
        val raw = ClipboardHelper.extractText(data)?.takeIf { it.isNotBlank() } ?: return
        Logger.d("ClipboardWatcher", "onPrimaryClipChanged received new clip")
        val snapshot = ClipSnapshot.capture(raw, data.description)
        latest.set(snapshot)

        if (policy.isSensitive(raw)) {
            Logger.w("ClipboardWatcher", "Clip content is sensitive, skipping classification/OTP")
            return
        }

        val type = classifier.classify(raw)
        if (type == ClipType.OTP) {
            otpCapture.maybeCapture(raw, snapshot)
        }
        // Other type branches (URL/URI/email) are routed through the
        // reactive pipeline elsewhere; this listener is intentionally the
        // minimal observation surface.
    }

    fun suppressPlatformPreview(suppress: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        Logger.i("ClipboardWatcher", "suppressPlatformPreview: $suppress")
        val current = clipboard.primaryClipDescription ?: return
        val extras = (current.extras ?: android.os.PersistableBundle().also { current.extras = it })
        if (suppress) {
            extras.putBoolean("android.content.extra.IS_SENSITIVE", true)
        } else {
            extras.remove("android.content.extra.IS_SENSITIVE")
        }
    }
}
