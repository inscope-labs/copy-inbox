package com.inscopelabs.abx.clipinbox.utils

import java.security.MessageDigest

object HashGenerator {
    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.trim().toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun sha1(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-1").digest(input.trim().toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
