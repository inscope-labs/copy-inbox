package com.inscopelabs.abx.clipinbox.cbxdag.link.delegation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DelegationCreationResultTest {

    @Test
    fun `parses delegationId and apiKey from response json`() {
        val json = """{"delegationId":"del_123","apiKey":"key_abc"}"""
        val result = DelegationCreationResult.fromJson(json)

        assertEquals("del_123", result.delegationId)
        assertEquals("key_abc", result.apiKey)
    }

    @Test
    fun `ignores unrelated extra fields`() {
        val json = """{"delegationId":"del_1","apiKey":"key_1","status":"ACTIVE"}"""
        val result = DelegationCreationResult.fromJson(json)

        assertEquals("del_1", result.delegationId)
        assertEquals("key_1", result.apiKey)
    }

    @Test
    fun `field order does not matter`() {
        val json = """{"apiKey":"key_9","delegationId":"del_9"}"""
        val result = DelegationCreationResult.fromJson(json)

        assertEquals("del_9", result.delegationId)
        assertEquals("key_9", result.apiKey)
    }

    @Test
    fun `throws when a required field is missing`() {
        assertThrows(IllegalArgumentException::class.java) {
            DelegationCreationResult.fromJson("""{"delegationId":"del_1"}""")
        }
    }
}
