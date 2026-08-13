package com.inscopelabs.abx.clipinbox.cbxdag.application.usecase

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node
import com.inscopelabs.abx.clipinbox.cbxdag.security.signing.ManifestSigner

class SignDagUseCase(
    private val signer: ManifestSigner
) {
    suspend fun execute(nodes: List<Node>): String {
        // Returns the signature of the manifest
        return signer.signManifest(nodes)
    }
}