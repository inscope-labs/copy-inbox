package com.inscopelabs.abx.clipinbox.cbxdag.application.engine

import com.inscopelabs.abx.clipinbox.cbxdag.application.usecase.*
import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Node
import com.inscopelabs.abx.clipinbox.cbxdag.domain.session.SessionBinder
import com.inscopelabs.abx.clipinbox.cbxdag.architecture.LifecycleContract

class DagLifecycleEngine(
    private val selectResources: SelectResourcesUseCase,
    private val buildDag: BuildDagUseCase,
    private val signDag: SignDagUseCase,
    private val pushDelegation: PushDelegationUseCase,
    private val sessionBinder: SessionBinder
) : LifecycleContract {

    override suspend fun execute(userPrompt: String?): String {
        // 1. SELECT – resources chosen by user
        val selected = selectResources.execute()

        // 2. RESOLVE – build resource nodes (already done inside selectResources)

        // 3. CREATE SESSION – bind prompt
        val session = sessionBinder.createSession(userPrompt)

        // 4. BUILD DAG – apply dependency inference
        val nodes = buildDag.execute(selected, userPrompt)

        // 5. VALIDATE – acyclic, orphan, capability, size (delegated to validators)

        // 6. HASH RESOURCES – compute hashes (delegated to hasher)

        // 7. SIGN – sign manifest (delegated to signer)
        val signature = signDag.execute(nodes)

        // 8. CREATE DELEGATION – build ephemeral envelope

        // 9. PUSH – to Cloudflare
        val delegationId = pushDelegation.execute(signature)

        // 10. ACTIVATE – CBX‑LINK now exposes resources (MCP + REST)

        // 11. (Background) TELEMETRY and HEARTBEAT start separately.

        return delegationId
    }
}