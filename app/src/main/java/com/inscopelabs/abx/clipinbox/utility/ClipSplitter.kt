package com.inscopelabs.abx.clipinbox.utility

enum class SplitMode {
    DELIMITER,
    FIXED_LENGTH
}

object ClipSplitter {
    fun split(content: String, mode: SplitMode, delimiter: String, chunkSize: Int): List<String> {
        val rawParts = when (mode) {
            SplitMode.DELIMITER -> {
                if (delimiter.isEmpty()) {
                    return listOf(content)
                }
                content.split(delimiter)
            }
            SplitMode.FIXED_LENGTH -> {
                if (chunkSize <= 0) {
                    return listOf(content)
                }
                content.chunked(chunkSize)
            }
        }

        val filtered = rawParts.map { it.trim() }.filter { it.isNotBlank() }
        if (filtered.size < 2) {
            return listOf(content)
        }
        return filtered
    }
}
