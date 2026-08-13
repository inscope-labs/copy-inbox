package com.inscopelabs.abx.clipinbox.cbxdag.application.usecase

import com.inscopelabs.abx.clipinbox.cbxdag.domain.dag.DagBuilder
import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node

class BuildDagUseCase(
    private val dagBuilder: DagBuilder
) {
    fun execute(selectedNodes: List<Node>, prompt: String?): List<Node> {
        return dagBuilder.build(selectedNodes, prompt)
    }
}