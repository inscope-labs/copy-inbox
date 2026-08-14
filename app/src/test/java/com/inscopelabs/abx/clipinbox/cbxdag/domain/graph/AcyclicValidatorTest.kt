package com.inscopelabs.abx.clipinbox.cbxdag.domain.graph

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AcyclicValidatorTest {

    private val validator = AcyclicValidator()

    private fun node(id: String, dependsOn: List<String>? = null) = Node(
        id = id,
        type = "clipboard-item",
        tier = "inline",
        hash = "sha256:test",
        capabilities = listOf("READ"),
        dependsOn = dependsOn
    )

    @Test
    fun `linear chain is acyclic`() {
        val nodes = listOf(
            node("a"),
            node("b", dependsOn = listOf("a")),
            node("c", dependsOn = listOf("b"))
        )
        assertTrue(validator.validate(nodes))
    }

    @Test
    fun `direct cycle is rejected`() {
        val nodes = listOf(
            node("a", dependsOn = listOf("b")),
            node("b", dependsOn = listOf("a"))
        )
        assertFalse(validator.validate(nodes))
    }

    @Test
    fun `self reference is rejected`() {
        val nodes = listOf(node("a", dependsOn = listOf("a")))
        assertFalse(validator.validate(nodes))
    }

    @Test
    fun `disconnected roots are acyclic`() {
        val nodes = listOf(node("a"), node("b"), node("c"))
        assertTrue(validator.validate(nodes))
    }
}
