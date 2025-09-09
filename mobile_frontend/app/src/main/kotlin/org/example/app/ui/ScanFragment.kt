package org.example.app.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.example.app.R
import org.example.app.utils.PermissionHelper

/**
 * ScanFragment lists nearby (and bonded) Bluetooth devices, allowing the user to "connect" (simulated).
 * Detected devices can be stored as preferred destinations for the Share screen.
 */
class ScanFragment : Fragment() {

    private lateinit var devicesList: RecyclerView
    private lateinit var scanButton: Button
    private lateinit var progress: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var adapter: DeviceAdapter
    private val devices: MutableList<BluetoothDevice> = mutableListOf()

    private val btAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null && devices.none { it.address == device.address }) {
                        devices.add(device)
                        adapter.notifyItemInserted(devices.size - 1)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    progress.visibility = View.GONE
                    scanButton.isEnabled = true
                }
            }
        }
    }

    // PUBLIC_INTERFACE
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**
         * Inflate the scan UI containing a list and scan control.
         */
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    // PUBLIC_INTERFACE
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /**
         * Initialize the device list and start a discovery scan if permissions allow.
         */
        super.onViewCreated(view, savedInstanceState)
        devicesList = view.findViewById(R.id.recycler_devices)
        scanButton = view.findViewById(R.id.btn_scan)
        progress = view.findViewById(R.id.progress)
        emptyText = view.findViewById(R.id.empty_devices)

        adapter = DeviceAdapter(
            items = devices,
            nameProvider = { d, ctx -> getDeviceDisplayName(d, ctx) },
            onClick = { device ->
                saveSelectedDevice(device)
                val name = getDeviceDisplayName(device)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.connected_to, name),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        devicesList.layoutManager = LinearLayoutManager(requireContext())
        devicesList.adapter = adapter

        scanButton.setOnClickListener { startDiscovery() }

        // Load bonded devices initially
        listBondedDevices()
    }

    // PUBLIC_INTERFACE
    override fun onResume() {
        /**
         * Register receivers for Bluetooth discovery events.
         */
        super.onResume()
        val f = IntentFilter()
        f.addAction(BluetoothDevice.ACTION_FOUND)
        f.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        requireActivity().registerReceiver(receiver, f)
    }

    // PUBLIC_INTERFACE
    override fun onPause() {
        /**
         * Unregister discovery receivers and cancel discovery if running.
         */
        super.onPause()
        try {
            requireActivity().unregisterReceiver(receiver)
        } catch (_: Exception) {}
        try {
            if (isScanPermissionGranted()) {
                btAdapter?.cancelDiscovery()
            }
        } catch (_: SecurityException) {}
    }

    private fun isConnectPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun isScanPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun listBondedDevices() {
        devices.clear()
        val bonded: Set<BluetoothDevice> = try {
            if (!isConnectPermissionGranted()) {
                emptySet()
            } else {
                btAdapter?.bondedDevices ?: emptySet()
            }
        } catch (_: SecurityException) {
            emptySet()
        }
        devices.addAll(bonded)
        adapter.notifyDataSetChanged()
        emptyText.visibility = if (devices.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun startDiscovery() {
        if (!PermissionHelper.hasAllPermissions(requireContext())) {
            PermissionHelper.requestAllPermissions(requireActivity(), 6001)
            return
        }
        if (btAdapter == null || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !btAdapter!!.isEnabled)) {
            Toast.makeText(requireContext(), "Bluetooth not available or disabled", Toast.LENGTH_SHORT).show()
            return
        }

        // Explicit permission checks for lint
        if (!isScanPermissionGranted() || !isConnectPermissionGranted()) {
            PermissionHelper.requestAllPermissions(requireActivity(), 6001)
            return
        }

        progress.visibility = View.VISIBLE
        scanButton.isEnabled = false
        try {
            btAdapter?.cancelDiscovery()
        } catch (_: SecurityException) { /* ignore */ }

        val started = try {
            btAdapter?.startDiscovery() == true
        } catch (_: SecurityException) {
            false
        }
        if (!started) {
            progress.visibility = View.GONE
            scanButton.isEnabled = true
            Toast.makeText(requireContext(), "Discovery start failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSelectedDevice(device: BluetoothDevice) {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val safeName = getDeviceDisplayName(device)
        prefs.edit {
            putString(KEY_DEVICE_NAME, safeName)
            putString(KEY_DEVICE_ADDR, device.address)
        }
    }

    private fun getDeviceDisplayName(device: BluetoothDevice, context: Context = requireContext()): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) device.name ?: device.address else device.address
            } else {
                device.name ?: device.address
            }
        } catch (_: SecurityException) {
            device.address
        }
    }

    private class DeviceAdapter(
        private val items: List<BluetoothDevice>,
        private val nameProvider: (BluetoothDevice, Context) -> String,
        private val onClick: (BluetoothDevice) -> Unit
    ) : RecyclerView.Adapter<DeviceAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.device_title)
            val subtitle: TextView = v.findViewById(R.id.device_subtitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val d = items[position]
            holder.title.text = nameProvider(d, holder.itemView.context)
            holder.subtitle.text = d.address
            holder.itemView.setOnClickListener { onClick(d) }
        }

        override fun getItemCount(): Int = items.size
    }

    companion object {
        private const val PREFS = "connectshare_prefs"
        private const val KEY_DEVICE_NAME = "selected_device_name"
        private const val KEY_DEVICE_ADDR = "selected_device_addr"
    }
}
