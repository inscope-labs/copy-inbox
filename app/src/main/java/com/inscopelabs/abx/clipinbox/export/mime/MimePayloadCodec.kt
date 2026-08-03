package com.inscopelabs.abx.clipinbox.export.mime

import android.os.Bundle
import android.os.Parcelable
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Base64

/**
 * Tiny codec for bundling typed payloads behind a single MIME key.
 *
 * Feature 3 — Custom MIME Exchange. Used by the FileProvider URI handoff
 * path so a single Intent extra can carry an envelope of (mime, bytes).
 *
 * Not a security boundary — sensitive payloads must be redacted before
 * reaching this codec.
 */
object MimePayloadCodec {

    private const val KEY_MIME = "mime"
    private const val KEY_BODY = "body"

    fun envelopeToBundle(mime: String, body: ByteArray): Bundle {
        Logger.d("MimePayloadCodec", "Creating envelope bundle for mime $mime (${body.size} bytes)")
        return Bundle().apply {
            putString(KEY_MIME, mime)
            putByteArray(KEY_BODY, body)
        }
    }

    fun envelopeFromBundle(bundle: Bundle?): Pair<String, ByteArray>? {
        if (bundle == null) {
            Logger.d("MimePayloadCodec", "envelopeFromBundle received null bundle")
            return null
        }
        val mime = bundle.getString(KEY_MIME) ?: return null
        val body = bundle.getByteArray(KEY_BODY) ?: return null
        Logger.d("MimePayloadCodec", "Extracted envelope for mime $mime (${body.size} bytes)")
        return mime to body
    }

    fun encodeParcelable(value: Parcelable): String {
        Logger.d("MimePayloadCodec", "Encoding parcelable")
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { it.writeObject(value) }
        return Base64.getEncoder().encodeToString(baos.toByteArray())
    }

    fun decodeParcelable(encoded: String): Parcelable? {
        Logger.d("MimePayloadCodec", "Decoding parcelable")
        return runCatching {
            val raw = Base64.getDecoder().decode(encoded)
            ObjectInputStream(ByteArrayInputStream(raw)).use { it.readObject() as Parcelable }
        }.getOrElse { error ->
            Logger.w("MimePayloadCodec", "Failed to decode parcelable: ${error.message}")
            null
        }
    }
}
