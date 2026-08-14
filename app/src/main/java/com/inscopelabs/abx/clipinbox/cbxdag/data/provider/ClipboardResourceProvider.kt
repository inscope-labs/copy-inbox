package com.inscopelabs.abx.clipinbox.cbxdag.data.provider

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node
import com.inscopelabs.abx.clipinbox.cbxdag.security.integrity.Sha256Hasher

class ClipboardResourceProvider(
    private val hasher: Sha256Hasher = Sha256Hasher()
) : ResourceProvider {
    override suspend fun resolve(resourceId: String): Node {
        // In a real app, get actual clipboard content. For POC, content is
        // a placeholder payload — see PlaceholderContent.
        val content = PlaceholderContent.bytesFor(resourceId, "clipboard-item")
        return Node(
            id = resourceId,
            type = "clipboard-item",
            tier = "inline",
            hash = hasher.hash(content),
            capabilities = listOf("read", "metadata")
        )
    }
}
