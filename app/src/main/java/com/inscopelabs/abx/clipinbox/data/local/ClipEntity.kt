package com.inscopelabs.abx.clipinbox.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clips")
data class ClipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val contentHash: String,
    val detectedType: String = "Text",
    val categoryId: Long = 0,
    val tags: String = "",
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isRead: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val charCount: Int = content.length,
    val wordCount: Int = if (content.isBlank()) 0 else content.trim().split("\\s+".toRegex()).size
)
