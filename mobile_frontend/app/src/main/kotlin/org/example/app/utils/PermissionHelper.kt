package org.example.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * PermissionHelper determines and requests the set of runtime permissions required for
 * Bluetooth scanning/connection and media access.
 */
object PermissionHelper {

    // PUBLIC_INTERFACE
    fun hasAllPermissions(context: Context): Boolean {
        /**
         * Check whether all required permissions have been granted.
         *
         * Parameters:
         * - context: Context to check permission state.
         *
         * Returns: true if all permissions are granted, false otherwise.
         */
        return requiredPermissions().all { p ->
            ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
        }
    }

    // PUBLIC_INTERFACE
    fun requestAllPermissions(activity: Activity, requestCode: Int) {
        /**
         * Request all required permissions in a single prompt.
         *
         * Parameters:
         * - activity: Activity to host the dialog.
         * - requestCode: Request code to receive results.
         */
        val perms = requiredPermissions()
        ActivityCompat.requestPermissions(activity, perms, requestCode)
    }

    // PUBLIC_INTERFACE
    fun requiredPermissions(): Array<String> {
        /**
         * Build the list of runtime permissions needed depending on API level.
         *
         * Returns: Array of permission strings.
         */
        val list = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list.add(Manifest.permission.BLUETOOTH_SCAN)
            list.add(Manifest.permission.BLUETOOTH_CONNECT)
            list.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        } else {
            list.add(Manifest.permission.BLUETOOTH)
            list.add(Manifest.permission.BLUETOOTH_ADMIN)
            list.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.READ_MEDIA_IMAGES)
            list.add(Manifest.permission.READ_MEDIA_VIDEO)
            list.add(Manifest.permission.READ_MEDIA_AUDIO)
            list.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            list.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        return list.toTypedArray()
        }
}
