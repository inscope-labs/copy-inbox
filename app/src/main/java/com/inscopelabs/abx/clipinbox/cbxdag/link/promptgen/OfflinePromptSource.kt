package com.inscopelabs.abx.clipinbox.cbxdag.link.promptgen

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Delegation

class OfflinePromptSource : PromptSource {
    override fun generate(delegation: Delegation): String {
        // Build a plain‑language instruction block for non‑MCP agents.
        return """
            You are an AI agent with access to the following resources via REST.
            Delegation ID: ${delegation.delegationId}
            API Key: (provided separately)
            Endpoint: https://cbx-link.dev/v1/delegations/${delegation.delegationId}/resources
            To list resources: GET /resources
            To read a resource: GET /resources/{nodeId}
            Available node IDs: ${delegation.manifest.nodes.joinToString { it.id }}
            Always include the API key in the Authorization header.
        """.trimIndent()
    }
}