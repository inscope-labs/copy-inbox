package com.inscopelabs.abx.clipinbox.cbxdag.link.delegation

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Manifest

/**
 * Builds the JSON POST body for `POST /v1/delegations`.
 *
 * POC scope: sends dag identity and signature only. Full manifest/resource
 * serialization (node list, capabilities, public key) is a follow-up once
 * a canonical wire format for Manifest/Node is designed — deliberately not
 * attempted here.
 *
 * Deliberately avoids org.json (an Android SDK class that isn't reliably
 * available under a plain JVM unit-test run without Robolectric) so this
 * stays genuinely testable in CI. Values are our own generated identifiers
 * (UUIDs, version strings) but are still escaped defensively.
 */
object DelegationRequestBody {
    fun build(manifest: Manifest, signature: String): String {
        return buildString {
            append('{')
            append("\"dagId\":").append(quote(manifest.dagId)).append(',')
            append("\"sessionId\":").append(quote(manifest.sessionId)).append(',')
            append("\"manifestVersion\":").append(quote(manifest.manifestVersion)).append(',')
            append("\"signature\":").append(quote(signature))
            append('}')
        }
    }

    private fun quote(value: String): String {
        val escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        return "\"$escaped\""
    }
}
