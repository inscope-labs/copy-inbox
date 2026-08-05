package com.inscopelabs.abx.clipinbox.utility

object ClipJoiner {
    fun join(parts: List<String>, separator: String): String = parts.joinToString(separator)
}
