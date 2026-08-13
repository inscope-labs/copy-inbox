package com.inscopelabs.abx.clipinbox.cbxdag.link.control

import com.inscopelabs.abx.clipinbox.cbxdag.architecture.DelegationAuthorityState

/**
 * Local state machine – mirrors remote authority for UI feedback.
 * Remote state is the source of truth; this is a cache.
 */
class DelegationStateMachine {
    private var currentState: DelegationAuthorityState = DelegationAuthorityState.ACTIVE

    fun transitionTo(newState: DelegationAuthorityState) {
        // Prevent transition out of terminal states.
        if (currentState == DelegationAuthorityState.REVOKED && newState != DelegationAuthorityState.REVOKED) {
            throw IllegalStateException("Cannot transition from REVOKED")
        }
        // EXPIRED is also terminal, but we allow updating to same.
        currentState = newState
    }

    fun getState(): DelegationAuthorityState = currentState
}