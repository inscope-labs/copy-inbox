package com.inscopelabs.abx.clipinbox.diagnostics

import android.content.Context
import android.util.Log
import java.io.File

object Logger {
    private var writer: LogWriter? = null

    fun initialize(context: Context) {
        writer = LogWriter(context)
        i("Logger", "Diagnostic Logger Initialized")
    }

    fun d(component: String, message: String) {
        Log.d(component, message)
        writer?.write("DEBUG", component, message)
    }

    fun i(component: String, message: String) {
        Log.i(component, message)
        writer?.write("INFO", component, message)
    }

    fun w(component: String, message: String, throwable: Throwable? = null) {
        Log.w(component, message, throwable)
        writer?.write("WARN", component, message, throwable)
    }

    fun e(component: String, message: String, throwable: Throwable? = null) {
        Log.e(component, message, throwable)
        writer?.write("ERROR", component, message, throwable)
    }

    fun getLogFile(): File? = writer?.mainLogFile
}
