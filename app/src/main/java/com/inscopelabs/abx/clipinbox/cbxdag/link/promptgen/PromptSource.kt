package com.inscopelabs.abx.clipinbox.cbxdag.link.promptgen

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Delegation

interface PromptSource {
    fun generate(delegation: Delegation): String
}