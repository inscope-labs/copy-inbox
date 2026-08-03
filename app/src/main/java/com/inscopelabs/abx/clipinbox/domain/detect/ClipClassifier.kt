package com.inscopelabs.abx.clipinbox.domain.detect

import com.inscopelabs.abx.clipinbox.diagnostics.Logger

class ClipClassifier {
    fun classify(text: String): ClipType {
        val trimmed = text.trim()
        val type = when {
            trimmed.matches(Regex("""^\d{4,8}$""")) -> ClipType.OTP
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> ClipType.URL
            trimmed.contains("@") && trimmed.contains(".") -> ClipType.EMAIL
            else -> ClipType.TEXT
        }
        Logger.d("ClipClassifier", "Classified clip as $type")
        return type
    }
}
