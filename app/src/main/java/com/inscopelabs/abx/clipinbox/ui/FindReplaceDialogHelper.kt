package com.inscopelabs.abx.clipinbox.ui

import android.content.Context
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.utility.FindReplaceEngine

object FindReplaceDialogHelper {

    private const val TAG = "FindReplaceDialogHelper"

    fun show(context: Context, currentText: String, onApply: (newText: String) -> Unit) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_find_replace, null)
        val etFind = view.findViewById<TextInputEditText>(R.id.et_find_text)
        val etReplace = view.findViewById<TextInputEditText>(R.id.et_replace_text)
        val cbUseRegex = view.findViewById<CheckBox>(R.id.cb_use_regex)

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.find_replace_title)
            .setView(view)
            .setPositiveButton(R.string.action_apply, null)
            .setNegativeButton(R.string.action_cancel) { d, _ ->
                d.dismiss()
            }
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val find = etFind.text?.toString().orEmpty()
            val replace = etReplace.text?.toString().orEmpty()
            val useRegex = cbUseRegex.isChecked

            val result = FindReplaceEngine.replace(currentText, find, replace, useRegex)
            result.onSuccess { newText ->
                Logger.i(TAG, "Find & replace succeeded: findLength=${find.length}, replaceLength=${replace.length}, useRegex=$useRegex")
                onApply(newText)
                dialog.dismiss()
            }.onFailure { exception ->
                val errorMsg = exception.message ?: context.getString(R.string.find_replace_invalid_pattern)
                Logger.w(TAG, "Find & replace failed: $errorMsg")
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
