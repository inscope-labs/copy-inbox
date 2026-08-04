package com.inscopelabs.abx.clipinbox.domain.queue

import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking

class QueueRepositoryImpl(
    private val dao: QueueDao,
) : ClipQueueManager.QueueRepository {

    override fun upsert(entity: QueueEntity): Long {
        Logger.d("QueueRepositoryImpl", "upsert entity name='${entity.suggestedName}', id=${entity.id}")
        return runBlocking(Dispatchers.IO) {
            dao.upsertSync(entity)
        }
    }

    override fun nextPending(limit: Int): List<QueueEntity> {
        Logger.d("QueueRepositoryImpl", "nextPending limit=$limit")
        return runBlocking(Dispatchers.IO) {
            dao.nextPending(limit)
        }
    }

    override fun markInFlight(id: Long) {
        Logger.d("QueueRepositoryImpl", "markInFlight id=$id")
        runBlocking(Dispatchers.IO) {
            dao.markInFlight(id)
        }
    }

    override fun markSent(id: Long) {
        Logger.d("QueueRepositoryImpl", "markSent id=$id")
        runBlocking(Dispatchers.IO) {
            dao.markSent(id)
        }
    }

    override fun markFailed(id: Long, error: String?) {
        Logger.d("QueueRepositoryImpl", "markFailed id=$id, error=$error")
        runBlocking(Dispatchers.IO) {
            dao.markFailed(id, error)
        }
    }

    fun observeAll(): Flow<List<QueueEntity>> {
        Logger.d("QueueRepositoryImpl", "observeAll")
        return dao.observeAll()
    }
}
