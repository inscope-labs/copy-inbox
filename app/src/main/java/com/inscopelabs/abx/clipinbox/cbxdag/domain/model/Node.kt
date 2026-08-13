package com.inscopelabs.abx.clipinbox.cbxdag.domain.model

data class Node(
    val id: String,
    val type: String,               // e.g., "clipboard-item", "csv", "pdf"
    val tier: String,               // "inline" or "attachment"
    val hash: String,               // "sha256:..."
    val capabilities: List<String>, // e.g., "read", "search"
    val dependsOn: List<String>? = null // node IDs this node depends on
)