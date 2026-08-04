package com.inscopelabs.abx.clipinbox.export.saf

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class SafExporter(private val context: Context) {

    // Save one clip to the given tree URI folder.
    // fileName must already be resolved (call MacroExpander first).
    // Returns the created document URI on success.
    suspend fun saveClip(
        clip: ClipEntity,
        treeUri: Uri,
        fileName: String,
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val docUri = DocumentsContract.buildDocumentUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri),
            )
            val created = DocumentsContract.createDocument(
                context.contentResolver,
                docUri,
                "text/plain",
                fileName,
            ) ?: return@withContext Result.failure(
                IOException("createDocument returned null for $fileName")
            )
            context.contentResolver.openOutputStream(created)?.use { out ->
                out.write(clip.content.toByteArray(Charsets.UTF_8))
            } ?: return@withContext Result.failure(
                IOException("openOutputStream returned null for $created")
            )
            Logger.i("SafExporter", "Saved ${clip.id} -> $created")
            Result.success(created)
        } catch (t: Throwable) {
            Logger.e("SafExporter", "save failed: ${t.message}")
            Result.failure(t)
        }
    }

    // Save multiple clips to the same folder, one file each.
    // Returns list of results in batchIndex order.
    suspend fun saveClips(
        clips: List<ClipEntity>,
        treeUri: Uri,
        fileNames: List<String>,
    ): List<Result<Uri>> {
        require(clips.size == fileNames.size) { "clips and fileNames must match in size" }
        return clips.zip(fileNames).map { (clip, name) ->
            saveClip(clip, treeUri, name)
        }
    }
}
