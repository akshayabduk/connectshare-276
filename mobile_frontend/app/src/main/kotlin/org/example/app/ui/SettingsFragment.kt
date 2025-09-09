package org.example.app.ui

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.Switch
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import org.example.app.R
import org.example.app.utils.PermissionHelper

/**
 * SettingsFragment allows toggling features and requesting permissions.
 */
class SettingsFragment : Fragment() {

    // PUBLIC_INTERFACE
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        /**
         * Inflate settings layout.
         */
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    // PUBLIC_INTERFACE
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /**
         * Wire toggles and buttons with SharedPreferences.
         */
        super.onViewCreated(view, savedInstanceState)
        val swAi: Switch = view.findViewById(R.id.sw_ai)
        val swBt: Switch = view.findViewById(R.id.sw_bt)
        val swWifi: Switch = view.findViewById(R.id.sw_wifi)
        val btnPerms: Button = view.findViewById(R.id.btn_permissions)

        val prefs = requireContext().getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
        swAi.isChecked = prefs.getBoolean(KEY_AI, true)
        swBt.isChecked = prefs.getBoolean(KEY_BT, true)
        swWifi.isChecked = prefs.getBoolean(KEY_WIFI, true)

        swAi.setOnCheckedChangeListener { _, isChecked -> prefs.edit { putBoolean(KEY_AI, isChecked) } }
        swBt.setOnCheckedChangeListener { _, isChecked -> prefs.edit { putBoolean(KEY_BT, isChecked) } }
        swWifi.setOnCheckedChangeListener { _, isChecked -> prefs.edit { putBoolean(KEY_WIFI, isChecked) } }

        btnPerms.setOnClickListener {
            PermissionHelper.requestAllPermissions(requireActivity(), 7001)
        }
    }

    companion object {
        private const val PREFS = "connectshare_prefs"
        private const val KEY_AI = "enable_ai"
        private const val KEY_BT = "enable_bt"
        private const val KEY_WIFI = "enable_wifi"
    }
}
