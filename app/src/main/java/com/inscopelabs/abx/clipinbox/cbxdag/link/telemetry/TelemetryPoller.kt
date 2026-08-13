package com.inscopelabs.abx.clipinbox.cbxdag.link.telemetry

import com.inscopelabs.abx.clipinbox.cbxdag.link.delegation.DelegationApiClient

class TelemetryPoller(
    private val apiClient: DelegationApiClient
) {
    suspend fun poll(delegationId: String, apiKey: String): TelemetryData {
        // GET /v1/delegations/{id}/telemetry
        // Return dummy data for POC
        return TelemetryData(
            status = "ACTIVE",
            agentCount = 2,
            sessionCount = 1,
            requestCount = 42
        )
    }
}

data class TelemetryData(
    val status: String,
    val agentCount: Int,
    val sessionCount: Int,
    val requestCount: Int
)