package com.inscopelabs.abx.clipinbox.cbxdag.data.provider

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node

class ClipboardResourceProvider : ResourceProvider {
    override suspend fun resolve(resourceId: String): Node {
        // In real app, get clipboard content.
        // For POC, return a dummy node.
        return Node(
            id = "clipboard_001",
            type = "clipboard-item",
            tier = "inline",
            hash = "sha256:dummy",
            capabilities = listOf("read", "metadata")
        )
    }
}