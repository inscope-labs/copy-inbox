package com.inscopelabs.abx.clipinbox.cbxdag.security.integrity

import java.security.MessageDigest

class Sha256Hasher {
    fun hash(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return "sha256:${hash.joinToString("") { "%02x".format(it) }}"
    }
}