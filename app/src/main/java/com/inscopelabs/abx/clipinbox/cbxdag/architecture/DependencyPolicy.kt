package com.inscopelabs.abx.clipinbox.cbxdag.architecture

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node

/**
 * Resolved decision (Section 4 of the plan):
 *
 *   - Default: user selection order defines the dependency graph.
 *   - Override: content/prompt‑based heuristics (optional, deferred).
 *
 * The `DependencyInferrer` must implement both paths behind one interface.
 */
interface DependencyPolicy {
    /**
     * Given a list of nodes and an optional prompt,
     * return the nodes with their `dependsOn` fields fully resolved.
     */
    fun apply(nodes: List<Node>, prompt: String?): List<Node>
}

/**
 * The default implementation uses user‑supplied order (the order of nodes
 * in the list) as the edge source. Heuristics are not applied in POC v0.1.
 */
class DefaultDependencyPolicy : DependencyPolicy {
    override fun apply(nodes: List<Node>, prompt: String?): List<Node> {
        // For POC, we assume dependencies were already set by the user
        // (e.g., via selection order). No automatic inference yet.
        // This preserves the explicit "selection‑order as default" rule.
        return nodes
    }
}

// A future override could implement content‑based heuristics.