package com.inscopelabs.abx.clipinbox.cbxdag.security.signing

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature

class ManifestSigner {
    suspend fun signManifest(nodes: List<Node>): String {
        // In POC, use Android Keystore; for simplicity, we return a dummy signature.
        // Real implementation would load the EC key and sign the manifest JSON bytes.
        return "dummy_signature_for_poc"
    }
}