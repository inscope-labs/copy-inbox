package com.inscopelabs.abx.clipinbox.cbxdag.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ManifestFactoryTest {

    private val factory = ManifestFactory()

    private fun node(id: String) = Node(
        id = id,
        type = "clipboard-item",
        tier = "inline",
        hash = "sha256:test",
        capabilities = listOf("READ")
    )

    @Test
    fun `manifest carries session id and prompt`() {
        val session = Session(sessionId = "sess-1", prompt = "summarize", createdAt = Instant.now())
        val manifest = factory.create(session, listOf(node("a")))

        assertEquals("sess-1", manifest.sessionId)
        assertEquals("summarize", manifest.prompt)
        assertEquals(1, manifest.nodes.size)
    }

    @Test
    fun `expiration is heartbeat interval after creation`() {
        val session = Session(sessionId = "sess-2", prompt = null, createdAt = Instant.now())
        val manifest = factory.create(session, emptyList(), heartbeatIntervalSeconds = 300)

        val actualGapSeconds = manifest.expiration.epochSecond - manifest.createdAt.epochSecond
        assertEquals(300L, actualGapSeconds)
    }

    @Test
    fun `each manifest gets a unique dag id`() {
        val session = Session(sessionId = "sess-3", prompt = null, createdAt = Instant.now())
        val first = factory.create(session, emptyList())
        val second = factory.create(session, emptyList())

        assertTrue(first.dagId != second.dagId)
    }
}
