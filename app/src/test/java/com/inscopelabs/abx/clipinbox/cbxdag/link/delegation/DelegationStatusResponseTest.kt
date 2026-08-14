package com.inscopelabs.abx.clipinbox.cbxdag.link.delegation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DelegationStatusResponseTest {

    @Test
    fun `parses all four fields from a real-shaped response`() {
        val json = """{"delegationId":"73cab65c-a44b-4fef-9943-12a8526fb3d2","status":"ACTIVE","createdAt":"2026-08-14T03:43:06.327Z","heartbeatAt":"2026-08-14T03:43:06.327Z"}"""
        val result = DelegationStatusResponse.fromJson(json)

        assertEquals("73cab65c-a44b-4fef-9943-12a8526fb3d2", result.delegationId)
        assertEquals("ACTIVE", result.status)
        assertEquals("2026-08-14T03:43:06.327Z", result.createdAt)
        assertEquals("2026-08-14T03:43:06.327Z", result.heartbeatAt)
    }

    @Test
    fun `parses a revoked status`() {
        val json = """{"delegationId":"d1","status":"REVOKED","createdAt":"t1","heartbeatAt":"t2"}"""
        assertEquals("REVOKED", DelegationStatusResponse.fromJson(json).status)
    }

    @Test
    fun `throws when a required field is missing`() {
        assertThrows(IllegalArgumentException::class.java) {
            DelegationStatusResponse.fromJson("""{"delegationId":"d1","status":"ACTIVE"}""")
        }
    }
}
