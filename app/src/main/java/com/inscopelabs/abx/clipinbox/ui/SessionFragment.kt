package com.inscopelabs.abx.clipinbox.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.inscopelabs.abx.clipinbox.ClipInBoxApplication
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.export.connector.RawSession

class SessionFragment : Fragment() {

    private lateinit var tvSessionStatus: TextView
    private lateinit var tvMailboxId: TextView
    private lateinit var btnConnect: Button
    private lateinit var btnDisconnect: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.i(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_session, container, false)

        tvSessionStatus = view.findViewById(R.id.tv_session_status)
        tvMailboxId = view.findViewById(R.id.tv_mailbox_id)
        btnConnect = view.findViewById(R.id.btn_connect)
        btnDisconnect = view.findViewById(R.id.btn_disconnect)

        btnConnect.setOnClickListener { showConnectDialog() }
        btnDisconnect.setOnClickListener { disconnectSession() }

        return view
    }

    override fun onResume() {
        super.onResume()
        Logger.d(TAG, "onResume: refreshing UI")
        refreshUi()
    }

    private fun refreshUi() {
        val app = requireActivity().application as ClipInBoxApplication
        val session = app.sessionGate.currentSessionOrNull()

        if (session != null) {
            Logger.d(TAG, "refreshUi: active session for mailbox ${session.mailboxId}")
            tvSessionStatus.text = getString(R.string.session_connected)
            tvSessionStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.pastel_green_archive)
            )
            tvMailboxId.text = getString(R.string.session_mailbox_prefix) + session.mailboxId
            tvMailboxId.visibility = View.VISIBLE
            btnConnect.visibility = View.GONE
            btnDisconnect.visibility = View.VISIBLE
        } else {
            Logger.d(TAG, "refreshUi: no active session")
            tvSessionStatus.text = getString(R.string.session_disconnected)
            tvSessionStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.gray_on_surface_variant)
            )
            tvMailboxId.visibility = View.GONE
            btnConnect.visibility = View.VISIBLE
            btnDisconnect.visibility = View.GONE
        }
    }

    private fun showConnectDialog() {
        Logger.i(TAG, "showConnectDialog: opening token entry dialog")
        val input = EditText(requireContext()).apply {
            hint = getString(R.string.session_token_hint)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.session_connect)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val raw = input.text.toString().trim()
                if (raw.isBlank()) {
                    Logger.w(TAG, "Connect failed: empty token input")
                    Toast.makeText(requireContext(), R.string.session_token_empty, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val parts = raw.split("|")
                if (parts.size != 3) {
                    Logger.w(TAG, "Connect failed: token format invalid (parts size != 3)")
                    Toast.makeText(requireContext(), "Invalid token format", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val expiresAt = parts[2].toLongOrNull()
                if (expiresAt == null) {
                    Logger.w(TAG, "Connect failed: expiresAt non-numeric")
                    Toast.makeText(requireContext(), "Invalid token format", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                try {
                    val app = requireActivity().application as ClipInBoxApplication
                    app.sessionGate.accept(RawSession(parts[0], parts[1], expiresAt))
                    Logger.i(TAG, "Connect success: session accepted for mailbox ${parts[1]}")
                    refreshUi()
                } catch (e: IllegalArgumentException) {
                    Logger.e(TAG, "Connect rejected by SessionGate: ${e.message}")
                    Toast.makeText(
                        requireContext(),
                        e.message ?: "Invalid session",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                Logger.d(TAG, "Connect dialog cancelled")
            }
            .show()
    }

    private fun disconnectSession() {
        Logger.i(TAG, "disconnectSession: revoking session")
        val app = requireActivity().application as ClipInBoxApplication
        app.sessionGate.revoke()
        refreshUi()
    }

    companion object {
        private const val TAG = "SessionFragment"
    }
}
