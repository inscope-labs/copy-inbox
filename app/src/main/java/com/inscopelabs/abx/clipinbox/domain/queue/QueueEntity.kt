package com.inscopelabs.abx.clipinbox.domain.queue

data class QueueEntity(
    val id: Long,
    val text: String,
    val timestamp: Long,
    val status: String = "PENDING"
)
