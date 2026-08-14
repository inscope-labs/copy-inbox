package com.inscopelabs.abx.clipinbox.cbxdag.domain.model

import com.inscopelabs.abx.clipinbox.cbxdag.architecture.DagPolicy
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * Builds a `Manifest` from a validated, dependency-resolved node list and
 * the session it belongs to. Centralizes dagId generation, versioning, and
 * expiration math so the lifecycle engine doesn't own construction details.
 *
 * Expiration is one heartbeat interval out from creation, matching
 * `DelegationAuthorityState.EXPIRED` semantics: the delegation lapses if
 * not refreshed by a heartbeat before that point (fail-closed).
 */
class ManifestFactory {
    fun create(
        session: Session,
        nodes: List<Node>,
        heartbeatIntervalSeconds: Int = DagPolicy.DEFAULT_HEARTBEAT_INTERVAL_SECONDS
    ): Manifest {
        val createdAt = Instant.now()
        return Manifest(
            manifestVersion = DagPolicy.MANIFEST_VERSION,
            dagId = UUID.randomUUID().toString(),
            sessionId = session.sessionId,
            createdAt = createdAt,
            prompt = session.prompt,
            nodes = nodes,
            expiration = createdAt.plus(heartbeatIntervalSeconds.toLong(), ChronoUnit.SECONDS),
            heartbeatIntervalSeconds = heartbeatIntervalSeconds
        )
    }
}
