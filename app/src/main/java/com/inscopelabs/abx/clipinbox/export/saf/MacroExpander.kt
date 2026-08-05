package com.inscopelabs.abx.clipinbox.export.saf

import com.inscopelabs.abx.clipinbox.data.local.ClipEntity
import com.inscopelabs.abx.clipinbox.data.local.SafPath

object MacroExpander {

    // Supported tokens:
    // {date}  — yyyyMMdd from clip.timestamp
    // {time}  — HHmmss from clip.timestamp
    // {type}  — clip.detectedType lowercased, spaces replaced with underscore
    // {index} — zero-padded position in batch (e.g. "01", "02"); pass batchIndex
    // {seq}   — zero-padded path.seqCounter + batchIndex (so each file in a
    //           batch gets a unique seq within the same save operation)
    // {hash}  — first 8 chars of clip.contentHash

    private val DATE_FMT = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
    private val TIME_FMT = java.text.SimpleDateFormat("HHmmss", java.util.Locale.US)

    fun expand(
        template: String,
        clip: ClipEntity,
        path: SafPath,
        batchIndex: Int = 0,
    ): String {
        val date = DATE_FMT.format(java.util.Date(clip.timestamp))
        val time = TIME_FMT.format(java.util.Date(clip.timestamp))
        val type = clip.detectedType.lowercase().replace(" ", "_")
        val index = batchIndex.toString().padStart(2, '0')
        val seq = (path.seqCounter + batchIndex).toString().padStart(4, '0')
        val hash = clip.contentHash.take(8)

        return template
            .replace("{date}", date)
            .replace("{time}", time)
            .replace("{type}", type)
            .replace("{index}", index)
            .replace("{seq}", seq)
            .replace("{hash}", hash)
            .sanitizeFilename()
    }

    // Strip characters not safe in Android document filenames
    private fun String.sanitizeFilename(): String =
        replace(Regex("[/\\\\:*?\"<>|]"), "_").trim()

    // Default filename when no macro is active
    fun defaultFilename(clip: ClipEntity, batchIndex: Int = 0): String =
        expand("clip_{date}_{time}_{index}", clip, SafPath(label = "", treeUri = ""),
            batchIndex)
}
