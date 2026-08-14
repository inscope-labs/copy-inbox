package com.inscopelabs.abx.clipinbox.cbxdag.data.provider

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node
import com.inscopelabs.abx.clipinbox.cbxdag.security.integrity.Sha256Hasher

class PdfResourceProvider(
    private val hasher: Sha256Hasher = Sha256Hasher()
) : ResourceProvider {
    override suspend fun resolve(resourceId: String): Node {
        // Placeholder payload pending real file-content reading — see
        // PlaceholderContent.
        val content = PlaceholderContent.bytesFor(resourceId, "pdf")
        return Node(
            id = resourceId,
            type = "pdf",
            tier = "attachment",
            hash = hasher.hash(content),
            capabilities = listOf("read", "metadata")
            // dependsOn is now left to the DependencyInferrer
        )
    }
}
