package com.inscopelabs.abx.clipinbox.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.inscopelabs.abx.clipinbox.ClipInBoxApplication
import com.inscopelabs.abx.clipinbox.R
import com.inscopelabs.abx.clipinbox.category.CategoryPreferences
import com.inscopelabs.abx.clipinbox.diagnostics.Logger
import com.inscopelabs.abx.clipinbox.service.overlay.OverlayPermissionGate
import com.inscopelabs.abx.clipinbox.service.overlay.OverlayService
import com.inscopelabs.abx.clipinbox.utils.NotificationPreferences

class SettingsFragment : Fragment() {

    private lateinit var switchPersistentNotification: SwitchMaterial
    private lateinit var switchCategoryDialog: SwitchMaterial
    private lateinit var switchOverlay: SwitchMaterial
    private lateinit var tvOverlayPermissionHint: TextView
    private lateinit var btnOverlayPermission: Button
    private lateinit var tvAutoClearDelay: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.i(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        switchPersistentNotification = view.findViewById(R.id.switch_persistent_notification)
        switchCategoryDialog = view.findViewById(R.id.switch_category_dialog)
        switchOverlay = view.findViewById(R.id.switch_overlay)
        tvOverlayPermissionHint = view.findViewById(R.id.tv_overlay_permission_hint)
        btnOverlayPermission = view.findViewById(R.id.btn_overlay_permission)
        tvAutoClearDelay = view.findViewById(R.id.tv_auto_clear_delay)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated: initializing settings state")

        switchPersistentNotification.isChecked =
            NotificationPreferences.isPersistentNotificationEnabled(requireContext())

        switchPersistentNotification.setOnCheckedChangeListener { _, isChecked ->
            Logger.i(TAG, "switchPersistentNotification changed: $isChecked")
            (requireActivity().application as ClipInBoxApplication)
                .setNotificationTriggerEnabled(isChecked)
        }

        switchCategoryDialog.isChecked =
            CategoryPreferences.isSaveDialogEnabled(requireContext())

        switchCategoryDialog.setOnCheckedChangeListener { _, isChecked ->
            Logger.i(TAG, "switchCategoryDialog changed: $isChecked")
            CategoryPreferences.setSaveDialogEnabled(requireContext(), isChecked)
        }

        switchOverlay.setOnCheckedChangeListener { _, isChecked ->
            Logger.i(TAG, "switchOverlay changed: $isChecked")
            if (isChecked) {
                val gate = OverlayPermissionGate(requireContext())
                if (gate.canDrawOverlays()) {
                    Logger.i(TAG, "Overlay permission granted, starting OverlayService")
                    OverlayService.start(requireContext())
                    tvOverlayPermissionHint.visibility = View.GONE
                    btnOverlayPermission.visibility = View.GONE
                } else {
                    Logger.w(TAG, "Overlay permission missing, reverting switch and showing prompt")
                    switchOverlay.isChecked = false
                    tvOverlayPermissionHint.visibility = View.VISIBLE
                    btnOverlayPermission.visibility = View.VISIBLE
                }
            } else {
                Logger.i(TAG, "Stopping OverlayService")
                OverlayService.stop(requireContext())
            }
        }

        btnOverlayPermission.setOnClickListener {
            Logger.i(TAG, "btnOverlayPermission clicked: requesting draw overlays permission")
            startActivity(OverlayPermissionGate(requireContext()).requestDrawOverlaysIntent())
        }
    }

    override fun onResume() {
        super.onResume()
        Logger.d(TAG, "onResume: refreshing overlay UI state")
        refreshOverlayUi()
    }

    private fun refreshOverlayUi() {
        val canDraw = OverlayPermissionGate(requireContext()).canDrawOverlays()
        Logger.d(TAG, "refreshOverlayUi: canDrawOverlays=$canDraw")
        if (canDraw) {
            btnOverlayPermission.visibility = View.GONE
            tvOverlayPermissionHint.visibility = View.GONE
        }
    }

    companion object {
        private const val TAG = "SettingsFragment"
    }
}
