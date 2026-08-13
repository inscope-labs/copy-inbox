package com.inscopelabs.abx.clipinbox.cbxdag.architecture

/**
 * The full POC lifecycle (Section 8 of the plan).
 * Each step is a distinct responsibility; the `DagLifecycleEngine` orchestrates them.
 *
 * Sequence:
 *   SELECT → RESOLVE → BUILD RESOURCE NODES → CALCULATE DEPENDENCIES → BUILD DAG
 *     → VALIDATE → HASH RESOURCES → SIGN → CREATE SESSION → CREATE DELEGATION
 *     → PUSH TO CLOUDFLARE → CBX‑LINK ACTIVATES → MCP AVAILABLE
 *     → AI READS RESOURCES → COPY INBOX POLLS TELEMETRY → COPY INBOX SENDS HEARTBEATS
 *     → USER TERMINATES → LOCAL REVOKED → REMOTE REVOKED → MCP ACCESS DENIED
 *
 * The engine must expose each phase as a distinct step, even if stubbed,
 * to preserve the invariant that the full sequence is understood.
 */
interface LifecycleContract {
    suspend fun execute(userPrompt: String?): String // returns delegationId
}