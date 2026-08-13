package com.inscopelabs.abx.clipinbox.cbxdag.architecture

/**
 * The authoritative state of a delegation is maintained remotely (Cloudflare).
 * Local state is only a mirror for UI responsiveness.
 *
 * States:
 *   - ACTIVE   : resources are readable.
 *   - REVOKED  : user explicitly terminated – remote denies reads.
 *   - EXPIRED  : heartbeat TTL elapsed without refresh – remote denies reads.
 *
 * Transitions:
 *   ACTIVE  → REVOKED (via /revoke)
 *   ACTIVE  → EXPIRED (automatically, when TTL expires)
 *   REVOKED and EXPIRED are terminal.
 *
 * Enforcement: All resource access (MCP or REST) MUST be denied by the
 * remote Durable Object when state is not ACTIVE.
 */
enum class DelegationAuthorityState {
    ACTIVE,
    REVOKED,
    EXPIRED
}

/**
 * Contract: remote state is authoritative.
 * Local changes must be confirmed by remote before being considered final.
 */
interface DelegationAuthority {
    suspend fun getRemoteState(delegationId: String, apiKey: String): DelegationAuthorityState
}