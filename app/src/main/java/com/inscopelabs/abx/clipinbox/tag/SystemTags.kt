package com.inscopelabs.abx.clipinbox.tag

object SystemTags {
    data class SystemTagSpec(
        val id: Long,
        val label: String,
        val colorHex: String
    )

    val PENDING = SystemTagSpec(id = 1L, label = "PENDING", colorHex = "#FF9800")
    val IN_PROGRESS = SystemTagSpec(id = 2L, label = "IN-PROGRESS", colorHex = "#2196F3")
    val COMPLETED = SystemTagSpec(id = 3L, label = "COMPLETED", colorHex = "#4CAF50")
    val BLOCKED = SystemTagSpec(id = 4L, label = "BLOCKED", colorHex = "#F44336")

    val ALL_SYSTEM_TAGS = listOf(PENDING, IN_PROGRESS, COMPLETED, BLOCKED)

    fun isSystemReservedLabel(label: String): Boolean {
        return ALL_SYSTEM_TAGS.any { it.label.equals(label, ignoreCase = true) }
    }

    fun isSystemReservedId(id: Long): Boolean {
        return ALL_SYSTEM_TAGS.any { it.id == id }
    }
}
