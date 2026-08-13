package com.inscopelabs.abx.clipinbox.cbxdag.security.signing

import java.io.File

class ArchiveSigner {
    fun signArchive(archiveFile: File): String {
        // Sign the entire .cbxdag archive (for integrity).
        return "dummy_archive_signature"
    }
}