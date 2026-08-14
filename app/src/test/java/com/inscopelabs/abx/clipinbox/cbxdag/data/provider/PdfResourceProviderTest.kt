package com.inscopelabs.abx.clipinbox.cbxdag.data.provider

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfResourceProviderTest {

    private val provider = PdfResourceProvider()

    @Test
    fun `resolve produces a real sha256 hash`() = runBlocking {
        val node = provider.resolve("financial_report")
        assertTrue(node.hash.startsWith("sha256:"))
        assertEquals(64, node.hash.removePrefix("sha256:").length)
        assertEquals("pdf", node.type)
        assertEquals("attachment", node.tier)
    }
}
