package com.inscopelabs.abx.clipinbox.cbxdag.data.repository

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Delegation

interface LocalDelegationRepository {
    suspend fun save(delegation: Delegation)
    suspend fun load(delegationId: String): Delegation?
    suspend fun delete(delegationId: String)
}

// For POC, we can implement with DataStore or Room, but we'll leave as interface.