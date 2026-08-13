package com.inscopelabs.abx.clipinbox.cbxdag.domain.model

import java.time.Instant

data class Manifest(
    val manifestVersion: String = "0.1",
    val dagId: String,
    val sessionId: String,
    val createdAt: Instant,
    val prompt: String? = null,
    val nodes: List<Node>,
    val expiration: Instant,
    val heartbeatIntervalSeconds: Int = 300
)