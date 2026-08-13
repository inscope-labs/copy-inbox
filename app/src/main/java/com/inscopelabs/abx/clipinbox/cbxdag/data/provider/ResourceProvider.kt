package com.inscopelabs.abx.clipinbox.cbxdag.data.provider

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node

interface ResourceProvider {
    suspend fun resolve(resourceId: String): Node
}