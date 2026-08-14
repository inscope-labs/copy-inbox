package com.inscopelabs.abx.clipinbox.cbxdag.data.provider

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardResourceProviderTest {

    private val provider = ClipboardResourceProvider()

    @Test
    fun `resolve uses the passed-in resourceId, not a hardcoded one`() = runBlocking {
        val node = provider.resolve("clip_42")
        assertEquals("clip_42", node.id)
    }

    @Test
    fun `resolve produces a real sha256 hash`() = runBlocking {
        val node = provider.resolve("clip_42")
        assertTrue(node.hash.startsWith("sha256:"))
        assertEquals(64, node.hash.removePrefix("sha256:").length)
    }

    @Test
    fun `same resourceId yields the same hash`() = runBlocking {
        val a = provider.resolve("clip_same")
        val b = provider.resolve("clip_same")
        assertEquals(a.hash, b.hash)
    }
}
