package com.inscopelabs.abx.clipinbox.export.uri

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.export.mime.CustomMimeTypes
import java.io.File

/**
 * Hands clip content off to other apps via FileProvider URIs.
 *
 * Feature 2 — URI Handoff. Wraps the underlying FileProvider so callers
 * just see `File -> Uri`.
 */
class ClipUriProvider(
    private val context: Context,
    private val authority: String = "${context.packageName}.fileprovider",
) {

    fun shareFile(file: File, mime: String = CustomMimeTypes.TEXT_PLAIN): Uri {
        require(file.exists()) { "file does not exist: ${file.absolutePath}" }
        Logger.i("ClipUriProvider", "Sharing file ${file.name} with mime $mime")
        return FileProvider.getUriForFile(context, authority, file).also { uri ->
            // Caller is responsible for granting read permission; we just make
            // sure the authority is the one we expect.
            require(uri.authority == authority) { "unexpected provider authority: ${uri.authority}" }
            @Suppress("UNUSED_VARIABLE")
            val _unused = mime // surfaced for caller convenience, not used here
        }
    }

    fun shareText(text: String, displayName: String): ShareHandle {
        Logger.i("ClipUriProvider", "Sharing text len=${text.length} as $displayName")
        val dir = File(context.cacheDir, "clip-share").apply { mkdirs() }
        val out = File(dir, displayName)
        out.writeText(text, Charsets.UTF_8)
        val uri = FileProvider.getUriForFile(context, authority, out)
        return ShareHandle(uri, out, CustomMimeTypes.TEXT_PLAIN)
    }

    data class ShareHandle(val uri: Uri, val file: File, val mime: String)
}
