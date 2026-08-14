package com.inscopelabs.abx.clipinbox.cbxdag.data.provider

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node
import com.inscopelabs.abx.clipinbox.cbxdag.security.integrity.Sha256Hasher

class ImageResourceProvider(
    private val hasher: Sha256Hasher = Sha256Hasher()
) : ResourceProvider {
    override suspend fun resolve(resourceId: String): Node {
        // Placeholder payload pending real file-content reading — see
        // PlaceholderContent.
        val content = PlaceholderContent.bytesFor(resourceId, "image")
        return Node(
            id = resourceId,
            type = "image",
            tier = "attachment",
            hash = hasher.hash(content),
            capabilities = listOf("read", "metadata")
        )
    }
}
