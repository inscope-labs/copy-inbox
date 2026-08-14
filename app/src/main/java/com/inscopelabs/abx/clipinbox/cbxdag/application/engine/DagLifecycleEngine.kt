package com.inscopelabs.abx.clipinbox.cbxdag.application.engine

import com.inscopelabs.abx.clipinbox.cbxdag.application.usecase.*
import com.inscopelabs.abx.clipinbox.cbxdag.application.validation.DagValidationResult
import com.inscopelabs.abx.clipinbox.cbxdag.application.validation.DagValidator
import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.ManifestFactory
import com.inscopelabs.abx.clipinbox.cbxdag.domain.session.SessionBinder
import com.inscopelabs.abx.clipinbox.cbxdag.architecture.LifecycleContract

class DagLifecycleEngine(
    private val selectResources: SelectResourcesUseCase,
    private val buildDag: BuildDagUseCase,
    private val signDag: SignDagUseCase,
    private val pushDelegation: PushDelegationUseCase,
    private val sessionBinder: SessionBinder,
    private val dagValidator: DagValidator = DagValidator(),
    private val manifestFactory: ManifestFactory = ManifestFactory()
) : LifecycleContract {

    override suspend fun execute(userPrompt: String?): String {
        // 1. SELECT – resources chosen by user
        val selected = selectResources.execute()

        // 2. RESOLVE – build resource nodes (already done inside selectResources)

        // 3. CREATE SESSION – bind prompt
        val session = sessionBinder.createSession(userPrompt)

        // 4. BUILD DAG – apply dependency inference
        val nodes = buildDag.execute(selected, userPrompt)

        // 5. VALIDATE – acyclic, orphan, capability, size
        val validation = dagValidator.validate(nodes)
        if (validation is DagValidationResult.Invalid) {
            throw IllegalStateException(
                "DAG validation failed: ${validation.reasons.joinToString("; ")}"
            )
        }

        // 6. HASH RESOURCES – node hashes are populated by the resource
        //    provider at resolution time (see ResourceProvider implementations).

        // 7. BUILD MANIFEST – assemble the validated, dependency-resolved DAG
        val manifest = manifestFactory.create(session, nodes)

        // 8. SIGN – sign manifest (delegated to signer)
        val signature = signDag.execute(manifest.nodes)

        // 9. CREATE DELEGATION – build ephemeral envelope (delegated to PushDelegationUseCase)

        // 10. PUSH – to Cloudflare
        val delegationId = pushDelegation.execute(signature)

        // 11. ACTIVATE – CBX-LINK now exposes resources (MCP + REST)

        // 12. (Background) TELEMETRY and HEARTBEAT start separately.

        return delegationId
    }
}
