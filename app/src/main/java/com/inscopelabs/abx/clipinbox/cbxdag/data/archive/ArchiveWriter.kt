package com.inscopelabs.abx.clipinbox.cbxdag.data.archive

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Manifest
import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node
import java.io.File

class ArchiveWriter {
    fun write(
        archiveDir: File,
        manifest: Manifest,
        nodes: List<Node>,
        signature: String
    ) {
        // Write manifest.json, nodes/*.json, signatures/*.sig
        // and copy attachments.
        // For POC, we just create the directory and write dummy files.
        archiveDir.mkdirs()
        val manifestFile = File(archiveDir, "manifest.json")
        manifestFile.writeText("""{"manifestVersion":"0.1"}""") // dummy
        // etc.
    }
}