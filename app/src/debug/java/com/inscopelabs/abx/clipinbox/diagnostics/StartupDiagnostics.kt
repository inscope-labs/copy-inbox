package com.inscopelabs.abx.clipinbox.diagnostics

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StartupDiagnostics {
    private val events = mutableListOf<String>()
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    @Synchronized
    fun recordEvent(event: String) {
        val timestamp = dateFormat.format(Date())
        events.add("[$timestamp] $event")
    }

    @Synchronized
    fun getTimeline(): String {
        return buildString {
            appendLine("=== STARTUP TIMELINE ===")
            events.forEach { appendLine(it) }
        }
    }
}
