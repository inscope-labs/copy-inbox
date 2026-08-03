package com.inscopelabs.abx.clipinbox.export.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.utils.HashGenerator

/**
 * Encodes arbitrary clip content into a Bitmap QR code.
 *
 * Feature 1 — QR Generator. The output is always square; callers pick a
 * size in pixels and a preset (which controls error correction).
 */
class QrEncoder(
    private val writer: QRCodeWriter = QRCodeWriter(),
) {

    fun encode(content: String, sizePx: Int, preset: QrPresetType = QrPresetType.STANDARD): Bitmap {
        require(sizePx in MIN_SIZE..MAX_SIZE) { "sizePx out of range" }
        Logger.i("QrEncoder", "Encoding QR code ($sizePx x $sizePx) preset=${preset.name} contentLen=${content.length}")
        val hints = hintsFor(preset)
        val matrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bmp = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
        for (x in 0 until matrix.width) {
            for (y in 0 until matrix.height) {
                bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    fun suggestFileName(content: String, preset: QrPresetType): String {
        val sig = HashGenerator.sha1(content).take(SIGNATURE_LEN)
        val filename = "qr-${preset.name.lowercase()}-$sig.png"
        Logger.d("QrEncoder", "Suggested QR filename $filename")
        return filename
    }

    private fun hintsFor(preset: QrPresetType): Map<EncodeHintType, Any> = when (preset) {
        QrPresetType.COMPACT -> mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L,
            EncodeHintType.MARGIN to 1,
        )
        QrPresetType.STANDARD -> mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 2,
        )
        QrPresetType.ROBUST -> mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to 2,
        )
        QrPresetType.PRINT -> mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.Q,
            EncodeHintType.MARGIN to 4,
        )
    }

    companion object {
        private const val MIN_SIZE = 96
        private const val MAX_SIZE = 2048
        private const val SIGNATURE_LEN = 10
    }
}
