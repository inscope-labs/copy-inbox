package com.inscopelabs.abx.clipinbox.ui

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.inscopelabs.abx.clipinbox.ClipInBoxApplication
import com.inscopelabs.abx.clipinbox.utils.ClipboardHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransparentCaptureActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = ClipboardHelper.getPrimaryClipText(this)
        if (text.isNullOrBlank()) {
            Toast.makeText(this, "Clipboard is empty!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val app = application as ClipInBoxApplication
        CoroutineScope(Dispatchers.IO).launch {
            val saved = app.repository.saveClipText(text)
            withContext(Dispatchers.Main) {
                if (saved) {
                    Toast.makeText(applicationContext, "Saved to ClipInBox!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Clip already exists in ClipInBox", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        }
    }
}
