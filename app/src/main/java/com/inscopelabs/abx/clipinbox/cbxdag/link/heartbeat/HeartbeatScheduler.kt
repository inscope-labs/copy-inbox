package com.inscopelabs.abx.clipinbox.cbxdag.link.heartbeat

import com.inscopelabs.abx.clipinbox.cbxdag.link.delegation.DelegationApiClient
import kotlinx.coroutines.*

class HeartbeatScheduler(
    private val apiClient: DelegationApiClient,
    private val intervalSeconds: Int = 300
) {
    private var job: Job? = null

    fun start(delegationId: String, apiKey: String) {
        job = CoroutineScope(Dispatchers.IO).launch {
            // Send first heartbeat immediately to establish lease.
            try {
                apiClient.sendHeartbeat(delegationId, apiKey)
            } catch (e: Exception) {
                // Log failure; remote will eventually expire if repeated failures.
            }
            while (isActive) {
                delay(intervalSeconds * 1000L)
                try {
                    apiClient.sendHeartbeat(delegationId, apiKey)
                } catch (e: Exception) {
                    // Continue retrying; remote TTL will expire if we fail too long.
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}