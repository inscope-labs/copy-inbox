package com.inscopelabs.abx.clipinbox.cbxdag.application.usecase

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Manifest
import com.inscopelabs.abx.clipinbox.cbxdag.link.delegation.DelegationApiClient
import com.inscopelabs.abx.clipinbox.cbxdag.security.credentials.ApiKeyManager

class PushDelegationUseCase(
    private val apiClient: DelegationApiClient,
    private val apiKeyManager: ApiKeyManager = ApiKeyManager()
) {
    suspend fun execute(manifest: Manifest, signature: String): String {
        val result = apiClient.createDelegation(manifest, signature)
        // Previously dropped entirely — the apiKey issued on creation is
        // required for every subsequent call (status/revoke/heartbeat).
        apiKeyManager.storeApiKey(result.delegationId, result.apiKey)
        return result.delegationId
    }
}
