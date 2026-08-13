package com.inscopelabs.abx.clipinbox.cbxdag.domain.dag

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node

class DagBuilder(
    private val inferrer: DependencyInferrer
) {
    fun build(nodes: List<Node>, prompt: String?): List<Node> {
        // Apply dependency inference (hybrid policy)
        return inferrer.infer(nodes, prompt)
    }
}