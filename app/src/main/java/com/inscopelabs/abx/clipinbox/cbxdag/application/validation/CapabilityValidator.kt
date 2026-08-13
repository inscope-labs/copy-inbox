package com.inscopelabs.abx.clipinbox.cbxdag.application.validation

import com.inscopelabs.abx.clipinbox.cbxdag.domain.capability.Capability
import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node

class CapabilityValidator {
    fun validate(nodes: List<Node>): Boolean {
        // Ensure each capability string maps to a known Capability enum
        val known = Capability.values().map { it.name }
        return nodes.all { node ->
            node.capabilities.all { it in known }
        }
    }
}