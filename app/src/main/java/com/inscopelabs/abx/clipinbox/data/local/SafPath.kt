package com.inscopelabs.abx.clipinbox.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saf_paths")
data class SafPath(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String,           // user-assigned display name
    val treeUri: String,         // persisted tree URI string
    val lastUsedAt: Long = 0L,   // epoch ms, updated on each successful save
    val seqCounter: Int = 0,     // monotonic counter, incremented per save to this path
)
