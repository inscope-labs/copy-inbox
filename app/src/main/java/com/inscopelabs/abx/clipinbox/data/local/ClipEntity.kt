package com.inscopelabs.abx.clipinbox.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clips")
data class ClipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val contentHash: String,
    val category: String = "Text",
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val charCount: Int = content.length,
    val wordCount: Int = if (content.isBlank()) 0 else content.trim().split("\\s+".toRegex()).size
)
