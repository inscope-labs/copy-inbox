package com.inscopelabs.abx.clipinbox.cbxdag.data.provider

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvResourceProviderTest {

    private val provider = CsvResourceProvider()

    @Test
    fun `resolve produces a real sha256 hash`() = runBlocking {
        val node = provider.resolve("sales_csv")
        assertTrue(node.hash.startsWith("sha256:"))
        assertEquals(64, node.hash.removePrefix("sha256:").length)
        assertEquals("csv", node.type)
        assertEquals("attachment", node.tier)
    }

    @Test
    fun `different resourceIds yield different hashes`() = runBlocking {
        val a = provider.resolve("sales_csv")
        val b = provider.resolve("other_csv")
        assertNotEquals(a.hash, b.hash)
    }
}
