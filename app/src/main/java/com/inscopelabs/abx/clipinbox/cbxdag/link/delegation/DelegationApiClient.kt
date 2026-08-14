package com.inscopelabs.abx.clipinbox.cbxdag.link.delegation

import com.inscopelabs.abx.clipinbox.cbxdag.architecture.CbxLinkConfig
import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Manifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * CBX‑LINK REST client.
 *
 * Important: The manifest signature proves DAG integrity. The API key
 * authenticates the caller (delegation owner) and is ISSUED by this
 * createDelegation call — it is not sent on creation, only received.
 * Subsequent calls (getStatus, revoke, heartbeat) use the issued key.
 */
class DelegationApiClient {

    suspend fun createDelegation(manifest: Manifest, signature: String): DelegationCreationResult =
        withContext(Dispatchers.IO) {
            val url = URL(CbxLinkConfig.BASE_URL + CbxLinkConfig.DELEGATIONS_PATH)
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")

                val body = DelegationRequestBody.build(manifest, signature)
                connection.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }

                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                val responseBody = stream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }

                if (responseCode !in 200..299) {
                    throw IllegalStateException(
                        "CBX-LINK createDelegation failed: HTTP $responseCode — $responseBody"
                    )
                }

                DelegationCreationResult.fromJson(responseBody)
            } finally {
                connection.disconnect()
            }
        }

    suspend fun getStatus(delegationId: String, apiKey: String): String {
        // TODO: GET /v1/delegations/{id} — Part D
        return "ACTIVE"
    }

    suspend fun revokeDelegation(delegationId: String, apiKey: String) {
        // TODO: POST /v1/delegations/{id}/revoke — Part D
    }

    suspend fun sendHeartbeat(delegationId: String, apiKey: String) {
        // TODO: POST /v1/delegations/{id}/heartbeat — Part D
    }
}
