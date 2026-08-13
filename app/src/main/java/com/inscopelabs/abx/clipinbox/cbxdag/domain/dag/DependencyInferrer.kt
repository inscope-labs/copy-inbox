package com.inscopelabs.abx.clipinbox.cbxdag.domain.dag

import com.inscopelabs.abx.clipinbox.cbxdag.architecture.DependencyPolicy
import com.inscopelabs.abx.clipinbox.cbxdag.architecture.DefaultDependencyPolicy
import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node

/**
 * Facade that applies the dependency policy.
 * The policy itself encapsulates the hybrid decision.
 */
class DependencyInferrer(
    private val policy: DependencyPolicy = DefaultDependencyPolicy()
) {
    fun infer(nodes: List<Node>, prompt: String?): List<Node> {
        return policy.apply(nodes, prompt)
    }
}