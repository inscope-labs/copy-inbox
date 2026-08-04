package com.inscopelabs.abx.clipinbox.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "naming_macros")
data class NamingMacro(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String,           // display name e.g. "Daily log"
    val template: String,        // e.g. "clip_{date}_{seq}"
    val createdAt: Long = System.currentTimeMillis(),
)
