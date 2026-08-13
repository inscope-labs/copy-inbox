package com.inscopelabs.abx.clipinbox.cbxdag.link.delegation

/**
 * CBX‑LINK REST client.
 *
 * Important: The manifest signature proves DAG integrity.
 * The API key authenticates the caller (delegation owner).
 * Both are required on creation; subsequent calls use the API key only.
 */
class DelegationApiClient {
    suspend fun createDelegation(signature: String): String {
        // TODO: Actual HTTP POST to /v1/delegations
        //   body includes manifest, resources, signature, and public key.
        //   Response includes delegationId and apiKey.
        // POC PLACEHOLDER – replace with real network call.
        return "delegation_123"
    }

    suspend fun getStatus(delegationId: String, apiKey: String): String {
        // TODO: GET /v1/delegations/{id}
        return "ACTIVE"
    }

    suspend fun revokeDelegation(delegationId: String, apiKey: String) {
        // TODO: POST /v1/delegations/{id}/revoke
    }

    suspend fun sendHeartbeat(delegationId: String, apiKey: String) {
        // TODO: POST /v1/delegations/{id}/heartbeat
    }
}