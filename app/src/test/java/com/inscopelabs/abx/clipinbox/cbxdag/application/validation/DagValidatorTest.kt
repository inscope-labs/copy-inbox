package com.inscopelabs.abx.clipinbox.cbxdag.application.validation

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DagValidatorTest {

    private val validator = DagValidator()

    private fun node(id: String, dependsOn: List<String>? = null, capabilities: List<String> = listOf("READ")) = Node(
        id = id,
        type = "clipboard-item",
        tier = "inline",
        hash = "sha256:test",
        capabilities = capabilities,
        dependsOn = dependsOn
    )

    @Test
    fun `valid dag passes with no reasons`() {
        val nodes = listOf(node("a"), node("b", dependsOn = listOf("a")))
        val result = validator.validate(nodes)
        assertTrue(result is DagValidationResult.Valid)
    }

    @Test
    fun `cycle produces a specific failure reason`() {
        val nodes = listOf(
            node("a", dependsOn = listOf("b")),
            node("b", dependsOn = listOf("a"))
        )
        val result = validator.validate(nodes) as DagValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("Cycle", ignoreCase = true) })
    }

    @Test
    fun `unknown capability produces a specific failure reason`() {
        val nodes = listOf(node("a", capabilities = listOf("NOT_A_REAL_CAPABILITY")))
        val result = validator.validate(nodes) as DagValidationResult.Invalid
        assertEquals(1, result.reasons.size)
        assertTrue(result.reasons.first().contains("capability", ignoreCase = true))
    }

    @Test
    fun `missing dependency produces a specific failure reason`() {
        val nodes = listOf(node("a", dependsOn = listOf("ghost")))
        val result = validator.validate(nodes) as DagValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("does not exist") })
    }
}
