package com.inscopelabs.abx.clipinbox.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.export.qr.QrEncoder
import com.inscopelabs.abx.clipinbox.export.qr.QrPresetType
import com.inscopelabs.abx.clipinbox.utils.ClipboardHelper

/**
 * Renders the current clipboard text as a QR code.
 *
 * Feature 1 — QR Generator.
 */
class QrFragment : Fragment() {

    private lateinit var encoder: QrEncoder
    private lateinit var preview: ImageView
    private var lastBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        Logger.i("QrFragment", "onCreateView")
        val root = inflater.inflate(R.layout.fragment_qr, container, false)
        preview = root.findViewById(R.id.qr_preview)
        return root
    }

    fun bind(encoder: QrEncoder) {
        this.encoder = encoder
    }

    fun renderFromClipboard(preset: QrPresetType = QrPresetType.STANDARD, sizePx: Int = 768) {
        val content = ClipboardHelper.read(requireContext()).orEmpty()
        if (content.isBlank()) {
            Logger.w("QrFragment", "Clipboard content blank, skipping QR render")
            return
        }
        Logger.i("QrFragment", "Rendering QR code for clipboard content len=${content.length}")
        val bmp = encoder.encode(content, sizePx, preset)
        lastBitmap?.recycle()
        lastBitmap = bmp
        preview.setImageBitmap(bmp)
    }

    override fun onDestroyView() {
        Logger.i("QrFragment", "onDestroyView")
        lastBitmap?.recycle()
        lastBitmap = null
        super.onDestroyView()
    }
}
