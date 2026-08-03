package com.inscopelabs.abx.clipinbox.domain.queue

import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.domain.detect.ClipClassifier
import com.inscopelabs.abx.clipinbox.domain.detect.ClipType
import com.inscopelabs.abx.clipinbox.domain.naming.ClipAutoNamer
import com.inscopelabs.abx.clipinbox.export.connector.FileManagerConnector
import com.inscopelabs.abx.clipinbox.export.connector.MailboxSendRequest
import com.inscopelabs.abx.clipinbox.export.connector.SessionGate
import com.inscopelabs.abx.clipinbox.security.SensitiveClipPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

/**
 * In-memory façade over the local queue table that also drives batch
 * dispatch to the abx-server mailbox connector.
 *
 * Feature 13 — Auto-Save + Batch Queue.
 */
class ClipQueueManager(
    private val repository: QueueRepository,
    private val classifier: ClipClassifier,
    private val namer: ClipAutoNamer,
    private val policy: SensitiveClipPolicy,
    private val sessionGate: SessionGate,
    private val connector: FileManagerConnector,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {

    fun enqueue(
        content: String,
        mime: String?,
        sourceUri: String?,
        now: Date = Date(),
    ): EnqueueResult {
        if (content.isBlank()) {
            Logger.w("ClipQueueManager", "Enqueue rejected: empty content")
            return EnqueueResult.Rejected("empty")
        }
        if (policy.isSensitive(content)) {
            Logger.w("ClipQueueManager", "Enqueue rejected: sensitive content")
            return EnqueueResult.Rejected("sensitive")
        }

        val type = classifier.classify(content)
        val name = namer.suggest(type, content, now)
        val entity = QueueEntity(
            suggestedName = name,
            type = type.name,
            content = content,
            mime = mime,
            sourceUri = sourceUri,
            createdAt = now.time,
        )
        val id = repository.upsert(entity)
        Logger.i("ClipQueueManager", "Enqueued clip #$id as '$name' (type $type)")
        return EnqueueResult.Accepted(id)
    }

    fun dispatchPending(batchSize: Int = DEFAULT_BATCH) {
        Logger.i("ClipQueueManager", "Dispatching pending queue items (batch size $batchSize)")
        scope.launch {
            val session = sessionGate.currentSessionOrNull() ?: run {
                Logger.w("ClipQueueManager", "Cannot dispatch: no active session")
                return@launch
            }
            val pending = repository.nextPending(batchSize)
            Logger.d("ClipQueueManager", "Found ${pending.size} pending items to dispatch")
            for (entity in pending) {
                repository.markInFlight(entity.id)
                val request = MailboxSendRequest(
                    session = session,
                    suggestedName = entity.suggestedName,
                    type = ClipType.valueOf(entity.type),
                    content = entity.content,
                    mime = entity.mime,
                    sourceUri = entity.sourceUri,
                )
                val outcome = connector.send(request)
                if (outcome.isSuccess) {
                    Logger.i("ClipQueueManager", "Successfully sent item #${entity.id}")
                    repository.markSent(entity.id)
                } else {
                    val errMsg = outcome.exceptionOrNull()?.message
                    Logger.e("ClipQueueManager", "Failed to send item #${entity.id}: $errMsg")
                    repository.markFailed(entity.id, errMsg)
                }
            }
        }
    }

    sealed interface EnqueueResult {
        data class Accepted(val id: Long) : EnqueueResult
        data class Rejected(val reason: String) : EnqueueResult
    }

    interface QueueRepository {
        fun upsert(entity: QueueEntity): Long
        fun nextPending(limit: Int): List<QueueEntity>
        fun markInFlight(id: Long)
        fun markSent(id: Long)
        fun markFailed(id: Long, error: String?)
    }

    companion object {
        const val DEFAULT_BATCH = 16
    }
}
