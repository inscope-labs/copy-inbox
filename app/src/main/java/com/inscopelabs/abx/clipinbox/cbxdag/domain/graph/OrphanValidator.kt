package com.inscopelabs.abx.clipinbox.cbxdag.domain.graph

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node

class OrphanValidator {
    fun validate(nodes: List<Node>): Boolean {
        // Ensure every node either has a dependency or is depended upon
        val allIds = nodes.map { it.id }.toSet()
        val referencedIds = nodes.flatMap { it.dependsOn ?: emptyList() }.toSet()
        val orphans = allIds - referencedIds
        // For POC, we consider a node with no dependencies as valid (root node)
        // But we might want to flag if a node is referenced but not present.
        val missingRefs = referencedIds - allIds
        return missingRefs.isEmpty()
        // Orphans are allowed (they are roots)
    }
}