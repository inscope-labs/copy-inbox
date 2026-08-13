package com.inscopelabs.abx.clipinbox.cbxdag.domain.session

import com.inscopelabs.abx.clipinbox.cbxdag.domain.model.Session
import java.time.Instant
import java.util.UUID

class SessionBinder {
    fun createSession(prompt: String?): Session {
        return Session(
            sessionId = UUID.randomUUID().toString(),
            prompt = prompt,
            createdAt = Instant.now()
        )
    }
}