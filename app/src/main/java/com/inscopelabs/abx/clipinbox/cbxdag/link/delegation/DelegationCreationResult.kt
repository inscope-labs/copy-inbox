package com.inscopelabs.abx.clipinbox.cbxdag.link.delegation

/**
 * Result of `POST /v1/delegations`. The apiKey is issued by CBX-LINK at
 * creation time and must be captured immediately — per the P0 design it
 * is not retrievable again after this response.
 *
 * Parsing deliberately avoids org.json (see DelegationRequestBody) so this
 * stays testable on a plain JVM without Robolectric.
 */
data class DelegationCreationResult(
    val delegationId: String,
    val apiKey: String
) {
    companion object {
        fun fromJson(json: String): DelegationCreationResult {
            return DelegationCreationResult(
                delegationId = extractStringField(json, "delegationId"),
                apiKey = extractStringField(json, "apiKey")
            )
        }

        private fun extractStringField(json: String, field: String): String {
            val regex = Regex("\"${Regex.escape(field)}\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
            val match = regex.find(json)
                ?: throw IllegalArgumentException("Field '$field' not found in response: $json")
            return match.groupValues[1]
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
        }
    }
}
