package com.inscopelabs.abx.clipinbox.diagnostics

object LogViewerAdapter {

    data class LogEntry(
        val timestamp: String,
        val level: String,
        val component: String,
        val message: String,
        val threadInfo: String = "",
        val session: String = ""
    )

    fun parseLogLines(lines: List<String>): List<LogEntry> {
        val entries = mutableListOf<LogEntry>()
        val regex = Regex("""^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})\s+\[(.*?)\]\s+\[(.*?)\]\s+\[SESS:(.*?)\]\s+\[(.*?)\]:\s+(.*)$""")

        var currentEntry: LogEntry? = null
        val messageBuilder = StringBuilder()

        for (line in lines) {
            val match = regex.matchEntire(line)
            if (match != null) {
                if (currentEntry != null) {
                    entries.add(currentEntry.copy(message = messageBuilder.toString().trimEnd()))
                    messageBuilder.clear()
                }

                val timestamp = match.groupValues[1]
                val threadInfo = match.groupValues[2]
                val level = match.groupValues[3]
                val session = match.groupValues[4]
                val component = match.groupValues[5]
                val messagePart = match.groupValues[6]

                currentEntry = LogEntry(
                    timestamp = timestamp,
                    level = level,
                    component = component,
                    message = "",
                    threadInfo = threadInfo,
                    session = session
                )
                messageBuilder.append(messagePart)
            } else {
                if (currentEntry != null) {
                    messageBuilder.append("\n").append(line)
                }
            }
        }

        if (currentEntry != null) {
            entries.add(currentEntry.copy(message = messageBuilder.toString().trimEnd()))
        }

        return entries
    }
}
