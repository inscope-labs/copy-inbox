package com.inscopelabs.abx.clipinbox.diagnostics

object LogSearchEngine {
    fun filterLogs(
        entries: List<LogViewerAdapter.LogEntry>,
        query: String,
        level: String
    ): List<LogViewerAdapter.LogEntry> {
        return entries.filter { entry ->
            val matchesLevel = if (level.equals("ALL", ignoreCase = true)) {
                true
            } else {
                entry.level.equals(level, ignoreCase = true)
            }

            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                entry.message.contains(query, ignoreCase = true) ||
                        entry.component.contains(query, ignoreCase = true) ||
                        entry.threadInfo.contains(query, ignoreCase = true) ||
                        entry.session.contains(query, ignoreCase = true)
            }

            matchesLevel && matchesQuery
        }
    }
}
