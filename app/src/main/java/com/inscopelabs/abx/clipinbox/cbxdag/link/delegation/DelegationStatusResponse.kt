package com.inscopelabs.abx.clipinbox.cbxdag.link.delegation

/**
 * Result of `GET /v1/delegations/{id}`. Field shape confirmed live against
 * the CBX-LINK Worker on 2026-08-14:
 * {"delegationId":"...","status":"ACTIVE","createdAt":"...","heartbeatAt":"..."}
 */
data class DelegationStatusResponse(
    val delegationId: String,
    val status: String,
    val createdAt: String,
    val heartbeatAt: String
) {
    companion object {
        fun fromJson(json: String): DelegationStatusResponse {
            return DelegationStatusResponse(
                delegationId = JsonField.extractString(json, "delegationId"),
                status = JsonField.extractString(json, "status"),
                createdAt = JsonField.extractString(json, "createdAt"),
                heartbeatAt = JsonField.extractString(json, "heartbeatAt")
            )
        }
    }
}
