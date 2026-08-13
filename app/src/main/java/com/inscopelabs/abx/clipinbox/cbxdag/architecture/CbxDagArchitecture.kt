package com.inscopelabs.abx.clipinbox.cbxdag.architecture

/**
 * CBX-DAG POC v0.1 – Architectural Overview
 *
 * This package defines the core architectural invariants that must be preserved
 * across all implementation phases.
 *
 * Key separations:
 *   - Durable Archive (.cbxdag)        ↔  Ephemeral Delegation (live on Cloudflare)
 *   - DAG Integrity (signature)        ↔  Caller Authentication (API key)
 *   - MCP Surface                      ↔  REST Mirror (identical authorization)
 *   - Local State (UI hint)            ↔  Remote Authority (source of truth)
 *
 * The POC proves that:
 *   1. A user can select resources → build a signed DAG → push a delegation.
 *   2. AI agents can consume resources via MCP or REST (with a generated prompt).
 *   3. The delegation can be explicitly revoked by the user.
 *   4. The delegation automatically expires when heartbeats cease (fail‑closed).
 *
 * See the implementation plan (DYNAMIC_DAG_POC_PLAN.md) for the full sequence.
 */
object CbxDagArchitecture {
    const val VERSION = "0.1"
}