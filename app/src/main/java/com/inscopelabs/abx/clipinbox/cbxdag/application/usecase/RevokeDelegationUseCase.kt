package com.inscopelabs.abx.clipinbox.cbxdag.application.usecase

import com.inscopelabs.abx.clipinbox.cbxdag.link.delegation.DelegationApiClient

class RevokeDelegationUseCase(
    private val apiClient: DelegationApiClient
) {
    suspend fun execute(delegationId: String, apiKey: String) {
        apiClient.revokeDelegation(delegationId, apiKey)
    }
}