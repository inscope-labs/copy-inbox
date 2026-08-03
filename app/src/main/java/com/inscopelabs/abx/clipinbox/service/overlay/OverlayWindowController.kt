package com.inscopelabs.abx.clipinbox.service.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inscopelabs.abx.clipinbox.diagnostics.Logger

/**
 * Owns the actual WindowManager layout params for the floating history
 * overlay.
 *
 * Feature 14 — Floating Clipboard History Overlay. The controller is the
 * only thing in the app that talks to WindowManager for the overlay
 * surface; everything else is plain view logic.
 */
class OverlayWindowController(
    private val context: Context,
    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager,
) {

    private var rootView: View? = null
    private var adapter: OverlayHistoryAdapter? = null

    fun show() {
        if (rootView != null) return
        Logger.i("OverlayWindowController", "Showing overlay window")
        val container = FrameLayout(context)
        val recycler = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = OverlayHistoryAdapter().also { this@OverlayWindowController.adapter = it }
        }
        container.addView(
            recycler,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )

        val params = layoutParams()
        windowManager.addView(container, params)
        rootView = container
    }

    fun hide() {
        val view = rootView ?: return
        Logger.i("OverlayWindowController", "Hiding overlay window")
        windowManager.removeView(view)
        rootView = null
        adapter = null
    }

    fun submit(items: List<OverlayHistoryAdapter.Item>) {
        Logger.d("OverlayWindowController", "Submitting ${items.size} items to overlay adapter")
        adapter?.submit(items)
    }

    private fun layoutParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }

    fun isShown(): Boolean = rootView != null

    @Suppress("unused")
    private fun inflater(): LayoutInflater = LayoutInflater.from(context)
}
