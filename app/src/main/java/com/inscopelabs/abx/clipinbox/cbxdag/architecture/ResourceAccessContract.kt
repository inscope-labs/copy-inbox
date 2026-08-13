package com.inscopelabs.abx.clipinbox.cbxdag.architecture

/**
 * The POC provides two surfaces for resource access:
 *   1. MCP (Model Context Protocol) – for MCP‑capable clients.
 *   2. REST – for non‑MCP agents (DeepSeek, Copilot, Grok, etc.).
 *
 * Both surfaces MUST share the **same authorization, capability, revocation,
 * and expiration enforcement** – implemented at the Durable Object layer.
 *
 * The generated prompt (OfflinePromptSource) instructs non‑MCP agents
 * to use the REST surface.
 */
interface ResourceAccessSurface {
    suspend fun listResources(delegationId: String, apiKey: String): List<ResourceMetadata>
    suspend fun readResource(delegationId: String, apiKey: String, nodeId: String): ResourceContent
}

interface McpResourceSurface : ResourceAccessSurface {
    // MCP‑specific handshake/transport details are implemented in the Worker.
    // The interface ensures the same operations exist.
}

interface RestResourceSurface : ResourceAccessSurface {
    // REST endpoints mirror the same operations.
}

data class ResourceMetadata(val id: String, val type: String, val capabilities: List<String>)
data class ResourceContent(val data: ByteArray, val contentType: String)