package com.inscopelabs.abx.clipinbox.diagnostics

import java.util.UUID

object SessionManager {
    var sessionId: String = UUID.randomUUID().toString().take(8)
        private set

    fun activateSession() {
        sessionId = UUID.randomUUID().toString().take(8)
    }
}
