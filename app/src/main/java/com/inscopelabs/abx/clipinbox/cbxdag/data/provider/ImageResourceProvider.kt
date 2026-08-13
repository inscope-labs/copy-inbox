package com.inscopelabs.abx.clipinbox.cbxdag.data.provider

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node

class ImageResourceProvider : ResourceProvider {
    override suspend fun resolve(resourceId: String): Node {
        return Node(
            id = resourceId,
            type = "image",
            tier = "attachment",
            hash = "sha256:dummy_image",
            capabilities = listOf("read", "metadata")
        )
    }
}