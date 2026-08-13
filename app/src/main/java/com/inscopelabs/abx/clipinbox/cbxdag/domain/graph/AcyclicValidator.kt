package com.inscopelabs.abx.clipinbox.cbxdag.domain.graph

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node

class AcyclicValidator {
    fun validate(nodes: List<Node>): Boolean {
        // Simple DFS cycle detection
        val graph = nodes.associate { it.id to (it.dependsOn ?: emptyList()) }
        val visited = mutableSetOf<String>() 
        val recursionStack = mutableSetOf<String>()

        fun dfs(nodeId: String): Boolean {
            if (nodeId in recursionStack) return false
            if (nodeId in visited) return true
            visited.add(nodeId)
            recursionStack.add(nodeId)
            for (dep in graph[nodeId] ?: emptyList()) {
                if (!dfs(dep)) return false
            }
            recursionStack.remove(nodeId)
            return true
        }

        return graph.keys.all { dfs(it) }
    }
}