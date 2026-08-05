package com.inscopelabs.abx.clipinbox.ui

import android.content.Context
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.utility.ClipSplitter
import com.inscopelabs.abx.clipinbox.utility.SplitMode

object SplitClipDialogHelper {

    private const val TAG = "SplitClipDialogHelper"

    fun show(
        context: Context,
        content: String,
        onConfirm: (parts: List<String>, deleteOriginal: Boolean) -> Unit
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_split_clip, null)
        val rgSplitMode = view.findViewById<RadioGroup>(R.id.rg_split_mode)
        val rbSplitDelimiter = view.findViewById<RadioButton>(R.id.rb_split_delimiter)
        val etSplitDelimiter = view.findViewById<TextInputEditText>(R.id.et_split_delimiter)
        val etSplitChunkSize = view.findViewById<TextInputEditText>(R.id.et_split_chunk_size)
        val cbDeleteOriginal = view.findViewById<CheckBox>(R.id.cb_delete_original)

        rgSplitMode.setOnCheckedChangeListener { _, checkedId ->
            val isDelimiter = checkedId == R.id.rb_split_delimiter
            etSplitDelimiter.isEnabled = isDelimiter
            etSplitChunkSize.isEnabled = !isDelimiter
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.split_clip_title)
            .setView(view)
            .setPositiveButton(R.string.action_split, null)
            .setNegativeButton(R.string.action_cancel) { d, _ ->
                d.dismiss()
            }
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val isDelimiterMode = rbSplitDelimiter.isChecked
            val mode = if (isDelimiterMode) SplitMode.DELIMITER else SplitMode.FIXED_LENGTH
            val delimiter = etSplitDelimiter.text?.toString().orEmpty()
            val chunkSizeStr = etSplitChunkSize.text?.toString().orEmpty()
            val chunkSize = chunkSizeStr.toIntOrNull() ?: 0
            val deleteOriginal = cbDeleteOriginal.isChecked

            if (mode == SplitMode.DELIMITER && delimiter.isEmpty()) {
                Logger.w(TAG, "Validation failed: Delimiter field is blank")
                Toast.makeText(context, R.string.split_delimiter_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (mode == SplitMode.FIXED_LENGTH && chunkSize <= 0) {
                Logger.w(TAG, "Validation failed: Chunk size invalid or <= 0 ($chunkSizeStr)")
                Toast.makeText(context, R.string.split_chunk_size_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val parts = ClipSplitter.split(content, mode, delimiter, chunkSize)
            if (parts.size < 2) {
                Logger.w(TAG, "Split produced < 2 parts (no real split)")
                Toast.makeText(context, R.string.split_no_effect, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Logger.i(TAG, "Split clip confirmed: mode=$mode, partCount=${parts.size}, deleteOriginal=$deleteOriginal")
            onConfirm(parts, deleteOriginal)
            dialog.dismiss()
        }
    }
}
