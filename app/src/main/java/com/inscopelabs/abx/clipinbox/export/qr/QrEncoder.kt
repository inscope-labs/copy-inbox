package com.inscopelabs.abx.clipinbox.export.qr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class QrEncoder {
    fun encode(content: String, sizePx: Int, preset: QrPresetType = QrPresetType.STANDARD): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply {
            color = if (preset == QrPresetType.INVERTED) Color.BLACK else Color.WHITE
        }
        val fgPaint = Paint().apply {
            color = if (preset == QrPresetType.INVERTED) Color.WHITE else Color.BLACK
        }

        canvas.drawRect(0f, 0f, sizePx.toFloat(), sizePx.toFloat(), bgPaint)

        val modules = 21
        val cellSize = sizePx / modules.toFloat()
        val hash = content.hashCode()

        for (row in 0 until modules) {
            for (col in 0 until modules) {
                val isFinderPattern = (row < 7 && col < 7) || (row < 7 && col >= modules - 7) || (row >= modules - 7 && col < 7)
                val fillCell = if (isFinderPattern) {
                    val r = if (row >= modules - 7) row - (modules - 7) else row
                    val c = if (col >= modules - 7) col - (modules - 7) else col
                    (r == 0 || r == 6 || c == 0 || c == 6) || (r in 2..4 && c in 2..4)
                } else {
                    ((hash xor (row * 31 + col * 17)) and 1) == 0
                }

                if (fillCell) {
                    canvas.drawRect(
                        col * cellSize,
                        row * cellSize,
                        (col + 1) * cellSize,
                        (row + 1) * cellSize,
                        fgPaint
                    )
                }
            }
        }
        return bitmap
    }
}
