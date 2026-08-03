package com.inscopelabs.abx.clipinbox.export

import android.content.Context
import android.net.Uri
import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.utils.TimeFormatter

object FileExporter {
    fun exportAsTxt(context: Context, clips: List<ClipEntity>, targetUri: Uri) {
        context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
            val writer = outputStream.bufferedWriter()
            clips.forEachIndexed { index, clip ->
                val header = "[${TimeFormatter.formatDetailedTime(clip.timestamp)}] (${clip.category})"
                writer.write(header)
                writer.newLine()
                writer.write(clip.content)
                writer.newLine()
                if (index < clips.size - 1) {
                    writer.write("---")
                    writer.newLine()
                    writer.newLine()
                }
            }
            writer.flush()
        }
    }
}
