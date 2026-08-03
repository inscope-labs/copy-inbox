package com.inscopelabs.abx.clipinbox.diagnostics

import android.content.Context
import java.io.File

object LogRotationManager {
    private const val MAX_LOG_SIZE_BYTES = 2L * 1024L * 1024L // 2MB
    private const val MAX_LOG_FILES = 5

    fun checkAndRotate(logFile: File) {
        if (logFile.length() < MAX_LOG_SIZE_BYTES) return

        val parentDir = logFile.parentFile ?: return

        // Delete oldest log if max log files limit reached
        val oldestFile = File(parentDir, "diagnostics_${MAX_LOG_FILES - 1}.log")
        if (oldestFile.exists()) {
            oldestFile.delete()
        }

        // Shift existing rotated files up by 1
        for (i in MAX_LOG_FILES - 2 downTo 1) {
            val file = File(parentDir, "diagnostics_$i.log")
            if (file.exists()) {
                val target = File(parentDir, "diagnostics_${i + 1}.log")
                file.renameTo(target)
            }
        }

        // Rename main file to diagnostics_1.log
        val firstRotated = File(parentDir, "diagnostics_1.log")
        logFile.renameTo(firstRotated)
    }

    fun getAllLogFiles(context: Context): List<File> {
        val logDir = File(context.filesDir, "logs")
        if (!logDir.exists()) return emptyList()

        return logDir.listFiles { _, name -> name.startsWith("diagnostics") }?.toList()?.sortedBy { it.name } ?: emptyList()
    }
}
