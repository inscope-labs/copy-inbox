package com.inscopelabs.abx.clipinbox.cbxdag.link.delegation

/**
 * Result of `POST /v1/delegations`. The apiKey is issued by CBX-LINK at
 * creation time and must be captured immediately — per the P0 design it
 * is not retrievable again after this response.
 */
data class DelegationCreationResult(
    val delegationId: String,
    val apiKey: String
) {
    companion object {
        fun fromJson(json: String): DelegationCreationResult {
            return DelegationCreationResult(
                delegationId = JsonField.extractString(json, "delegationId"),
                apiKey = JsonField.extractString(json, "apiKey")
            )
        }
    }
}
