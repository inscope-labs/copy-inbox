package com.inscopelabs.abx.clipinbox.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.URLUtil

object ClipboardHelper {
    fun getPrimaryClipText(context: Context): String? {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clipData = clipboardManager?.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val text = clipData.getItemAt(0).text?.toString()
            if (!text.isNull_orEmpty()) {
                return text
            }
        }
        return null
    }

    private fun CharSequence?.isNull_orEmpty(): Boolean {
        return this == null || this.isEmpty()
    }

    fun copyToClipboard(context: Context, text: String, label: String = "ClipInBox"): Boolean {
        return try {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboardManager?.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun detectCategory(text: String): String {
        val trimmed = text.trim()
        if (URLUtil.isValidUrl(trimmed) || trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("www.")) {
            return "Link"
        }
        
        val codeKeywords = listOf("class ", "fun ", "def ", "function", "var ", "val ", "const ", "import ", "<html", "import React", "public static void", "SELECT ", "{", "}")
        val hasCodePattern = codeKeywords.any { trimmed.contains(it) } || trimmed.contains(";") && trimmed.contains("{")
        if (hasCodePattern && trimmed.length > 10) {
            return "Code"
        }

        if (trimmed.length > 250 || trimmed.contains("\n\n")) {
            return "Note"
        }

        return "Text"
    }
}
