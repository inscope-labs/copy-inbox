package com.inscopelabs.abx.clipinbox.cbxdag.domain.model

import java.time.Instant

data class Delegation(
    val delegationId: String,
    val dagId: String,
    val sessionId: String,
    val manifest: Manifest,
    val resources: List<ResourcePayload>, // inline data or attachment pointers
    val capabilities: List<String>,
    val expiration: Instant,
    val heartbeatIntervalSeconds: Int,
    val signature: String // signed manifest hash
)

data class ResourcePayload(
    val nodeId: String,
    val inlineData: String? = null,       // base64 or plain text for small resources
    val attachmentUrl: String? = null     // R2 URL for larger resources
)