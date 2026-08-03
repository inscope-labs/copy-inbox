package com.inscopelabs.abx.clipinbox.diagnostics

import android.app.ActivityManager
import android.content.Context
import android.os.Debug

object RuntimeDiagnostics {
    fun captureSnapshot(context: Context): String {
        val runtime = Runtime.getRuntime()
        val usedMemMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val maxMemMb = runtime.maxMemory() / (1024 * 1024)
        
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        return buildString {
            appendLine("=== RUNTIME DIAGNOSTICS SNAPSHOT ===")
            appendLine("Heap Used Memory : $usedMemMb MB / $maxMemMb MB")
            appendLine("Native Heap Size : ${Debug.getNativeHeapAllocatedSize() / (1024 * 1024)} MB")
            if (memoryInfo != null) {
                appendLine("System Avail Mem : ${memoryInfo.availMem / (1024 * 1024)} MB")
                appendLine("System Low Mem   : ${memoryInfo.lowMemory}")
            }
            appendLine("Active Threads   : ${Thread.activeCount()}")
        }
    }
}
