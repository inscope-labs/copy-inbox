package com.inscopelabs.abx.clipinbox.cbxdag.data.provider

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node

class FileResourceProvider : ResourceProvider {
    override suspend fun resolve(resourceId: String): Node {
        // For POC, resolve based on id.
        return when (resourceId) {
            "sales_csv" -> CsvResourceProvider().resolve(resourceId)
            "financial_report" -> PdfResourceProvider().resolve(resourceId)
            else -> throw IllegalArgumentException("Unknown file resource")
        }
    }
}