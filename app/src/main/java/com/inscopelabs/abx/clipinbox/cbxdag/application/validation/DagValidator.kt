package com.inscopelabs.abx.clipinbox.cbxdag.application.validation

import com.inscopelabs.abx.clipinbox.cbxdag.domain.graph.AcyclicValidator
import com.inscopelabs.abx.clipinbox.cbxdag.domain.graph.OrphanValidator
import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node

sealed class DagValidationResult {
    object Valid : DagValidationResult()
    data class Invalid(val reasons: List<String>) : DagValidationResult()
}

/**
 * Combines the structural (domain.graph) and policy (application.validation)
 * validators into a single pass over a built DAG. This is Section 8, step
 * "VALIDATE" of the lifecycle — previously each validator existed but none
 * were ever invoked together.
 *
 * Order: cycles are checked first, since a cyclic graph makes orphan,
 * capability, and size checks meaningless.
 */
class DagValidator(
    private val acyclicValidator: AcyclicValidator = AcyclicValidator(),
    private val orphanValidator: OrphanValidator = OrphanValidator(),
    private val capabilityValidator: CapabilityValidator = CapabilityValidator(),
    private val sizeTierValidator: SizeTierValidator = SizeTierValidator()
) {
    fun validate(nodes: List<Node>): DagValidationResult {
        val reasons = mutableListOf<String>()

        if (!acyclicValidator.validate(nodes)) {
            reasons += "Cycle detected in dependency graph"
        }
        if (!orphanValidator.validate(nodes)) {
            reasons += "Node references a dependency that does not exist"
        }
        if (!capabilityValidator.validate(nodes)) {
            reasons += "Node declares an unknown capability"
        }
        if (!sizeTierValidator.validate(nodes)) {
            reasons += "One or more nodes exceed configured size limits"
        }

        return if (reasons.isEmpty()) {
            DagValidationResult.Valid
        } else {
            DagValidationResult.Invalid(reasons)
        }
    }
}
