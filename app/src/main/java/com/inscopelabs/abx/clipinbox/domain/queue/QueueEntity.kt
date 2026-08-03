package com.inscopelabs.abx.clipinbox.domain.queue

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A queued clip awaiting batch send to the abx-server mailbox.
 *
 * Feature 13 — Auto-Save + Batch Queue. Tier 2 send path.
 */
@Entity(tableName = "queue")
data class QueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val suggestedName: String,
    val type: String,
    val content: String,
    val mime: String?,
    val sourceUri: String?,
    val createdAt: Long,
    val attempts: Int = 0,
    val lastError: String? = null,
    val state: State = State.PENDING,
) {
    enum class State { PENDING, IN_FLIGHT, SENT, FAILED }
}
