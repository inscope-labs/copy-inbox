package com.inscopelabs.abx.clipinbox.domain.queue

import com.inscopelabs.abx.clipinbox.diagnostics.Logger

class ClipQueueManager {
    fun dispatchPending() {
        Logger.i("ClipQueueManager", "Dispatching pending queued items")
    }
}
