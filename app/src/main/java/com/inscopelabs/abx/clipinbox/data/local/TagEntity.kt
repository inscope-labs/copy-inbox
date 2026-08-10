package com.inscopelabs.abx.clipinbox.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val colorHex: String,
    val isSystemReserved: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Long
)
