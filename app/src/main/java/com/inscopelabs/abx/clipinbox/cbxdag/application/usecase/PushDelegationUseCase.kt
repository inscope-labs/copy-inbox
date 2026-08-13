package com.inscopelabs.abx.clipinbox.cbxdag.application.usecase

import com.inscopelabs.abx.clipinbox.cbxdag.link.delegation.DelegationApiClient

class PushDelegationUseCase(
    private val apiClient: DelegationApiClient
) {
    suspend fun execute(signature: String): String {
        // Build full delegation payload and push
        return apiClient.createDelegation(signature)
    }
}