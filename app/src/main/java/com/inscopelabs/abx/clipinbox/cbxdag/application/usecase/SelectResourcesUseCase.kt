package com.inscopelabs.abx.clipinbox.cbxdag.application.usecase

import com.inscopelabs.abx.clipinbox.cbxdag.data.provider.ResourceProvider
import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node

class SelectResourcesUseCase(
    private val provider: ResourceProvider
) {
    suspend fun execute(): List<Node> {
        // POC fixed set – dependencies will be inferred by DagBuilder.
        return listOf(
            provider.resolve("clipboard_001"),
            provider.resolve("sales_csv"),
            provider.resolve("financial_report")
        )
    }
}