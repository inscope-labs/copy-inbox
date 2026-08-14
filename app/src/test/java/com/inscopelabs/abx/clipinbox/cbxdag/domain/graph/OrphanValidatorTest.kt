package com.inscopelabs.abx.clipinbox.cbxdag.domain.graph

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrphanValidatorTest {

    private val validator = OrphanValidator()

    private fun node(id: String, dependsOn: List<String>? = null) = Node(
        id = id,
        type = "clipboard-item",
        tier = "inline",
        hash = "sha256:test",
        capabilities = listOf("READ"),
        dependsOn = dependsOn
    )

    @Test
    fun `root nodes with no dependencies are valid`() {
        val nodes = listOf(node("a"), node("b"))
        assertTrue(validator.validate(nodes))
    }

    @Test
    fun `dependency present in node set is valid`() {
        val nodes = listOf(node("a"), node("b", dependsOn = listOf("a")))
        assertTrue(validator.validate(nodes))
    }

    @Test
    fun `dependency on a missing node is rejected`() {
        val nodes = listOf(node("a", dependsOn = listOf("does_not_exist")))
        assertFalse(validator.validate(nodes))
    }
}
