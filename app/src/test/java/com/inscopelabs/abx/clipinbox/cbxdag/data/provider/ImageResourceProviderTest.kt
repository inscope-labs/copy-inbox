package com.inscopelabs.abx.clipinbox.cbxdag.data.provider

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageResourceProviderTest {

    private val provider = ImageResourceProvider()

    @Test
    fun `resolve produces a real sha256 hash`() = runBlocking {
        val node = provider.resolve("photo_001")
        assertTrue(node.hash.startsWith("sha256:"))
        assertEquals(64, node.hash.removePrefix("sha256:").length)
        assertEquals("image", node.type)
        assertEquals("attachment", node.tier)
    }
}
