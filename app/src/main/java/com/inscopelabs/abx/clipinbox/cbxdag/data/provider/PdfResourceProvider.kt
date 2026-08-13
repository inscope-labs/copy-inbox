package com.inscopelabs.abx.clipinbox.cbxdag.data.provider

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node

class PdfResourceProvider : ResourceProvider {
    override suspend fun resolve(resourceId: String): Node {
        return Node(
            id = resourceId,
            type = "pdf",
            tier = "attachment",
            hash = "sha256:dummy_pdf",
            capabilities = listOf("read", "metadata")
            // dependsOn is now left to the DependencyInferrer
        )
    }
}