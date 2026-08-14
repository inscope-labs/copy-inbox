package com.inscopelabs.abx.clipinbox.cbxdag.link.delegation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class JsonFieldTest {

    @Test
    fun `extracts a simple string field`() {
        val json = """{"status":"ACTIVE"}"""
        assertEquals("ACTIVE", JsonField.extractString(json, "status"))
    }

    @Test
    fun `extractStringOrNull returns null for missing field`() {
        val json = """{"status":"ACTIVE"}"""
        assertNull(JsonField.extractStringOrNull(json, "missing"))
    }

    @Test
    fun `extractString throws for missing field`() {
        assertThrows(IllegalArgumentException::class.java) {
            JsonField.extractString("""{"status":"ACTIVE"}""", "missing")
        }
    }

    @Test
    fun `handles escaped quotes and backslashes in value`() {
        val json = """{"note":"a \"quoted\" value with \\backslash"}"""
        assertEquals("a \"quoted\" value with \\backslash", JsonField.extractString(json, "note"))
    }

    @Test
    fun `field order in the json does not matter`() {
        val json = """{"a":"1","b":"2","c":"3"}"""
        assertEquals("2", JsonField.extractString(json, "b"))
    }
}
