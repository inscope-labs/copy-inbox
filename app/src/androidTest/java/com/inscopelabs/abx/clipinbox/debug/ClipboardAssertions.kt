package com.inscopelabs.abx.clipinbox.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

/**
 * Assertion helpers for clipboard behavior in instrumentation tests.
 *
 * Feature 11 — Clipboard Assertion Testing. Lives in `androidTest` so it
 * can drive the real platform clipboard from a connected device.
 */
object ClipboardAssertions {

    private val clipboard: ClipboardManager by lazy {
        InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    fun assertPrimaryClipEquals(expected: String) {
        val actual = clipboard.primaryClip?.getItemAt(0)?.coerceToText(
            InstrumentationRegistry.getInstrumentation().targetContext,
        )?.toString()
        Logger.d("ClipboardAssertions", "Asserting primary clip equals expected: $expected, actual: $actual")
        assertEquals(expected, actual)
    }

    fun assertPrimaryClipIsOtp() {
        val raw = clipboard.primaryClip?.getItemAt(0)?.coerceToText(
            InstrumentationRegistry.getInstrumentation().targetContext,
        )?.toString().orEmpty()
        Logger.d("ClipboardAssertions", "Asserting primary clip is OTP, raw: $raw")
        assertNotNull(raw)
        assertTrue("expected OTP, got '$raw'", raw.matches(Regex("""^\d{4,8}$""")))
    }

    fun assertHasMime(expectedMime: String) {
        val desc = clipboard.primaryClipDescription
        Logger.d("ClipboardAssertions", "Asserting has MIME: $expectedMime, actual: ${desc?.getMimeType(0)}")
        assertNotNull("no primary clip description", desc)
        assertTrue(
            "expected mime $expectedMime, got ${desc?.getMimeType(0)}",
            desc?.getMimeType(0) == expectedMime,
        )
    }

    fun push(text: String, mime: String = "text/plain") {
        Logger.i("ClipboardAssertions", "Pushing test clip with mime $mime")
        val clip = ClipData.newPlainText("test", text).apply {
            description.extras = android.os.PersistableBundle().apply {
                putString("mime", mime)
            }
        }
        clipboard.setPrimaryClip(clip)
    }
}
