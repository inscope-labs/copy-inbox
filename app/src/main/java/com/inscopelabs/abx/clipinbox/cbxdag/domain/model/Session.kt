package com.inscopelabs.abx.clipinbox.cbxdag.domain.model

import java.time.Instant

data class Session(
    val sessionId: String,
    val prompt: String?,
    val createdAt: Instant
)