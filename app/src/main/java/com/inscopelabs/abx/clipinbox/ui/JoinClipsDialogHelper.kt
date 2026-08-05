package com.inscopelabs.abx.clipinbox.ui

import android.content.Context
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.diagnostics.Logger

object JoinClipsDialogHelper {

    private const val TAG = "JoinClipsDialogHelper"

    fun show(
        context: Context,
        clipCount: Int,
        onConfirm: (separator: String, deleteOriginals: Boolean) -> Unit
    ) {
        val density = context.resources.displayMetrics.density
        val paddingPx = (16 * density).toInt()

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val til = TextInputLayout(context, null, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (12 * density).toInt()
            }
        }

        val etSeparator = TextInputEditText(til.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            hint = context.getString(R.string.split_delimiter_hint)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setText("\n\n")
        }
        til.addView(etSeparator)
        layout.addView(til)

        val cbDeleteOriginals = CheckBox(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = context.getString(R.string.split_delete_original)
            isChecked = false
        }
        layout.addView(cbDeleteOriginals)

        AlertDialog.Builder(context)
            .setTitle(R.string.join_clips_title)
            .setMessage(context.getString(R.string.join_clips_message_format, clipCount))
            .setView(layout)
            .setPositiveButton(R.string.action_join) { _, _ ->
                val separator = etSeparator.text?.toString().orEmpty()
                val deleteOriginals = cbDeleteOriginals.isChecked
                Logger.i(TAG, "Join clips confirmed: clipCount=$clipCount, deleteOriginals=$deleteOriginals")
                onConfirm(separator, deleteOriginals)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
}
