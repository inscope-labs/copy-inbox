package com.inscopelabs.abx.clipinbox.cbxdag.link.delegation

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Manifest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class DelegationRequestBodyTest {

    private fun manifest(dagId: String = "dag-abc") = Manifest(
        manifestVersion = "0.1",
        dagId = dagId,
        sessionId = "sess-xyz",
        createdAt = Instant.parse("2026-08-14T00:00:00Z"),
        prompt = "summarize",
        nodes = emptyList(),
        expiration = Instant.parse("2026-08-14T00:05:00Z"),
        heartbeatIntervalSeconds = 300
    )

    @Test
    fun `body contains dag identity and signature`() {
        val json = DelegationRequestBody.build(manifest(), "sig-123")

        assertTrue(json.contains("\"dagId\":\"dag-abc\""))
        assertTrue(json.contains("\"sessionId\":\"sess-xyz\""))
        assertTrue(json.contains("\"manifestVersion\":\"0.1\""))
        assertTrue(json.contains("\"signature\":\"sig-123\""))
    }

    @Test
    fun `quotes and backslashes in values are escaped`() {
        val json = DelegationRequestBody.build(manifest(dagId = "dag\"with\\chars"), "sig-123")

        assertTrue(json.contains("\"dagId\":\"dag\\\"with\\\\chars\""))
    }
}
