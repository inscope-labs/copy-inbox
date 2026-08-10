package com.inscopelabs.abx.clipinbox.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "clip_tag_cross_ref",
    primaryKeys = ["clipId", "tagId"],
    indices = [Index(value = ["tagId"])]
)
data class ClipTagCrossRef(
    val clipId: Long,
    val tagId: Long
)
