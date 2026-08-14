package com.inscopelabs.abx.clipinbox.cbxdag.security.integrity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Sha256HasherTest {

    private val hasher = Sha256Hasher()

    @Test
    fun `hash is prefixed and 64 hex chars`() {
        val result = hasher.hash("hello".toByteArray())
        assertTrue(result.startsWith("sha256:"))
        assertEquals(64, result.removePrefix("sha256:").length)
    }

    @Test
    fun `same input produces same hash`() {
        val a = hasher.hash("same input".toByteArray())
        val b = hasher.hash("same input".toByteArray())
        assertEquals(a, b)
    }

    @Test
    fun `different input produces different hash`() {
        val a = hasher.hash("input a".toByteArray())
        val b = hasher.hash("input b".toByteArray())
        assertTrue(a != b)
    }

    @Test
    fun `known vector matches standard sha256`() {
        // sha256("abc") is a well-known test vector.
        val result = hasher.hash("abc".toByteArray())
        assertEquals(
            "sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            result
        )
    }
}
