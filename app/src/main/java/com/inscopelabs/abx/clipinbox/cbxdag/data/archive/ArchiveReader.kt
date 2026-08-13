package com.inscopelabs.abx.clipinbox.cbxdag.data.archive

import java.io.File

class ArchiveReader {
    fun read(archiveFile: File): ByteArray {
        // For POC, simply read the file bytes
        return archiveFile.readBytes()
    }
}