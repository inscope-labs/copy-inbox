package com.inscopelabs.abx.clipinbox.diagnostics

import android.content.Context
import java.io.File
import java.io.FileWriter

class LogWriter(context: Context) {
    private val logDir = File(context.filesDir, "logs").apply { if (!exists()) mkdirs() }
    val mainLogFile = File(logDir, "diagnostics.log")

    @Synchronized
    fun write(level: String, component: String, message: String, throwable: Throwable? = null) {
        val formatted = LogFormatter.format(level, component, message, throwable)
        
        LogRotationManager.checkAndRotate(mainLogFile)

        FileWriter(mainLogFile, true).use { writer ->
            writer.write(formatted)
            writer.write("\n")
        }
    }
}
