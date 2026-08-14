package com.inscopelabs.abx.clipinbox.cbxdag.link.delegation

import com.inscopelabs.abx.clipinbox.cbxdag.architecture.CbxLinkConfig
import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Manifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * CBX-LINK REST client.
 *
 * The manifest signature proves DAG integrity. The API key authenticates
 * the caller (delegation owner) and is ISSUED by createDelegation — it is
 * not sent on creation, only received. Every other call requires it via
 * the X-CBX-API-Key header (confirmed live against the Worker on
 * 2026-08-14: creation skips auth, status/heartbeat/revoke all require it).
 */
class DelegationApiClient {

    suspend fun createDelegation(manifest: Manifest, signature: String): DelegationCreationResult {
        val body = DelegationRequestBody.build(manifest, signature)
        val (_, responseBody) = request("POST", CbxLinkConfig.DELEGATIONS_PATH, apiKey = null, body = body)
        return DelegationCreationResult.fromJson(responseBody)
    }

    suspend fun getStatus(delegationId: String, apiKey: String): String {
        val (_, responseBody) = request("GET", "${CbxLinkConfig.DELEGATIONS_PATH}/$delegationId", apiKey)
        return DelegationStatusResponse.fromJson(responseBody).status
    }

    suspend fun revokeDelegation(delegationId: String, apiKey: String) {
        request("POST", "${CbxLinkConfig.DELEGATIONS_PATH}/$delegationId/revoke", apiKey)
    }

    suspend fun sendHeartbeat(delegationId: String, apiKey: String) {
        request("POST", "${CbxLinkConfig.DELEGATIONS_PATH}/$delegationId/heartbeat", apiKey)
    }

    private suspend fun request(
        method: String,
        path: String,
        apiKey: String?,
        body: String? = null
    ): Pair<Int, String> = withContext(Dispatchers.IO) {
        val url = URL(CbxLinkConfig.BASE_URL + path)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.setRequestProperty("Content-Type", "application/json")
            apiKey?.let { connection.setRequestProperty("X-CBX-API-Key", it) }

            if (body != null) {
                connection.doOutput = true
                connection.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseBody = stream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }

            if (responseCode !in 200..299) {
                throw IllegalStateException("CBX-LINK $method $path failed: HTTP $responseCode — $responseBody")
            }

            responseCode to responseBody
        } finally {
            connection.disconnect()
        }
    }
}
