package com.inscopelabs.abx.clipinbox.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.ChipGroup
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.export.qr.QrEncoder
import com.inscopelabs.abx.clipinbox.export.qr.QrPresetType
import com.inscopelabs.abx.clipinbox.export.uri.ClipUriProvider
import com.inscopelabs.abx.clipinbox.utils.ClipboardHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Renders the current clipboard text as a QR code.
 *
 * Feature 1 — QR Generator.
 */
class QrFragment : Fragment() {

    private lateinit var encoder: QrEncoder
    private lateinit var qrPreview: ImageView
    private lateinit var btnGenerate: Button
    private lateinit var btnShareQr: Button
    private lateinit var tvEmptyHint: TextView
    private lateinit var chipGroupPreset: ChipGroup
    private var selectedPreset: QrPresetType = QrPresetType.STANDARD
    private var lastBitmap: Bitmap? = null
    private var lastClipContent: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        Logger.i("QrFragment", "onCreateView")
        val root = inflater.inflate(R.layout.fragment_qr, container, false)
        qrPreview = root.findViewById(R.id.qr_preview)
        btnGenerate = root.findViewById(R.id.btn_generate)
        btnShareQr = root.findViewById(R.id.btn_share_qr)
        tvEmptyHint = root.findViewById(R.id.tv_empty_hint)
        chipGroupPreset = root.findViewById(R.id.chip_group_preset)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.i("QrFragment", "onViewCreated")
        encoder = QrEncoder()

        chipGroupPreset.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: -1
            selectedPreset = when (checkedId) {
                R.id.chip_compact -> QrPresetType.COMPACT
                R.id.chip_standard -> QrPresetType.STANDARD
                R.id.chip_robust -> QrPresetType.ROBUST
                R.id.chip_print -> QrPresetType.PRINT
                else -> QrPresetType.STANDARD
            }
            Logger.d("QrFragment", "Selected preset: ${selectedPreset.name}")
        }

        btnGenerate.setOnClickListener {
            val content = ClipboardHelper.read(requireContext()).orEmpty()
            if (content.isBlank()) {
                Logger.w("QrFragment", "Clipboard content blank")
                Toast.makeText(requireContext(), R.string.qr_empty_clipboard, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Logger.i("QrFragment", "Generating QR code len=${content.length} preset=${selectedPreset.name}")
            lifecycleScope.launch {
                val bmp = withContext(Dispatchers.Default) {
                    encoder.encode(content, 768, selectedPreset)
                }
                lastBitmap?.recycle()
                lastBitmap = bmp
                lastClipContent = content
                qrPreview.setImageBitmap(bmp)
                btnShareQr.visibility = View.VISIBLE
                tvEmptyHint.visibility = View.GONE
            }
        }

        btnShareQr.setOnClickListener {
            val bitmap = lastBitmap ?: return@setOnClickListener
            Logger.i("QrFragment", "Sharing QR code image")
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val dir = File(requireContext().cacheDir, "clip-share").apply { mkdirs() }
                    val file = File(dir, encoder.suggestFileName(lastClipContent, selectedPreset))
                    file.outputStream().use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                    val uri = ClipUriProvider(requireContext()).shareFile(file, "image/png")
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    withContext(Dispatchers.Main) {
                        startActivity(
                            Intent.createChooser(
                                intent,
                                getString(R.string.qr_share_chooser_title)
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        Logger.i("QrFragment", "onDestroyView")
        lastBitmap?.recycle()
        lastBitmap = null
        super.onDestroyView()
    }
}
