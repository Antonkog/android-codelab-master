package com.sap.codelab.utils.permissions

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.sap.codelab.R

/**
 * Centralized runtime permissions handler for location and notification permissions.
 * Must be created with an Activity that is also an ActivityResultCaller (e.g., AppCompatActivity).
 */
class PermissionsHandler(
    private val activity: Activity,
    caller: ActivityResultCaller,
) {

    interface Callback {
        fun onNotificationPermissionGranted()
        fun onNotificationPermissionDenied()
        fun onAllLocationPermissionsGranted()
        fun onLocationPermissionDenied()
    }

    var callback: Callback? = null

    // Enum to track which permission flow triggered the settings screen
    private enum class PermissionFlow {
        FOREGROUND_LOCATION,
        BACKGROUND_LOCATION,
        NOTIFICATION
    }

    private var pendingPermissionFlow: PermissionFlow? = null

    private val requestFineLocationLauncher: ActivityResultLauncher<String> =
        caller.registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            if (hasAccessLocationPermissions()) {
                requestBackgroundLocation()
            } else {
                pendingPermissionFlow = PermissionFlow.FOREGROUND_LOCATION
                handlePermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val requestBackgroundLocationLauncher: ActivityResultLauncher<String> =
        caller.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                pendingPermissionFlow = PermissionFlow.BACKGROUND_LOCATION
                handlePermissionDenied(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                callback?.onAllLocationPermissionsGranted()
                ensureNotificationPermission()
            }
        }

    private val requestPostNotificationsLauncher: ActivityResultLauncher<String> =
        caller.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                callback?.onNotificationPermissionGranted()
            } else {
                pendingPermissionFlow = PermissionFlow.NOTIFICATION
                showNonCancelablePermissionDeniedDialog(Manifest.permission.POST_NOTIFICATIONS)
                callback?.onNotificationPermissionDenied()
            }
        }

    // Launcher to detect return from settings
    private val settingsLauncher: ActivityResultLauncher<Intent> =
        caller.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Recheck permissions based on the flow that triggered settings
            when (pendingPermissionFlow) {
                PermissionFlow.FOREGROUND_LOCATION -> ensureLocationPermissions()
                PermissionFlow.BACKGROUND_LOCATION -> requestBackgroundLocation()
                PermissionFlow.NOTIFICATION -> ensureNotificationPermission()
                null -> {} // No pending flow
            }
            pendingPermissionFlow = null // Reset after handling
        }

    fun ensureLocationPermissions() {
        if (!hasAccessLocationPermissions()) {
            showDialog(
                title = activity.getString(R.string.permission_location_title),
                message = activity.getString(R.string.permission_location_rationale),
                onContinue = {
                    requestFineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                onDenied = {
                    pendingPermissionFlow = PermissionFlow.FOREGROUND_LOCATION
                    handlePermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            )
            return
        }
        requestBackgroundLocation()
    }

    private fun requestBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val perm = Manifest.permission.ACCESS_BACKGROUND_LOCATION
            if (!isGranted(perm)) {
                showDialog(
                    title = activity.getString(R.string.permission_background_location_title),
                    message = activity.getString(R.string.permission_background_location_rationale) +
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                "\n\nNote: Please select 'Allow all the time' in settings."
                            } else "",
                    onContinue = {
                        requestBackgroundLocationLauncher.launch(perm)
                    },
                    onDenied = {
                        pendingPermissionFlow = PermissionFlow.BACKGROUND_LOCATION
                        handlePermissionDenied(perm)
                    }
                )
                return
            }
        }
        callback?.onAllLocationPermissionsGranted()
        ensureNotificationPermission()
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = Manifest.permission.POST_NOTIFICATIONS
            if (isGranted(perm)) {
                callback?.onNotificationPermissionGranted()
            } else if (activity.shouldShowRequestPermissionRationale(perm)) {
                showDialog(
                    title = activity.getString(R.string.permission_notification_title),
                    message = activity.getString(R.string.permission_notification_rationale),
                    onContinue = {
                        requestPostNotificationsLauncher.launch(perm)
                    },
                    onDenied = {
                        pendingPermissionFlow = PermissionFlow.NOTIFICATION
                        callback?.onNotificationPermissionDenied()
                        showNonCancelablePermissionDeniedDialog(perm)
                    }
                )
            } else {
                requestPostNotificationsLauncher.launch(perm)
            }
        } else {
            callback?.onNotificationPermissionGranted()
        }
    }

    private fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun hasAccessLocationPermissions(): Boolean {
        return isGranted(Manifest.permission.ACCESS_FINE_LOCATION) || isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun showDialog(
        title: String,
        message: String,
        onContinue: () -> Unit,
        onDenied: () -> Unit
    ) {
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(R.string.permission_not_now) { _, _ -> onDenied() }
            .setPositiveButton(R.string.permission_continue) { _, _ -> onContinue() }
            .setCancelable(true)
            .show()
    }

    private fun showNonCancelablePermissionDeniedDialog(permission: String) {
        val message = when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION ->
                activity.getString(R.string.permission_location_denied)

            Manifest.permission.ACCESS_BACKGROUND_LOCATION ->
                activity.getString(R.string.permission_background_location_denied)

            Manifest.permission.POST_NOTIFICATIONS ->
                activity.getString(R.string.permission_notification_denied)

            else -> activity.getString(R.string.permission_generic_denied)
        }
        AlertDialog.Builder(activity)
            .setTitle(R.string.permission_denied_title)
            .setMessage(message)
            .setPositiveButton(R.string.permission_settings) { _, _ ->
                openAppSettings()
            }
            .setCancelable(false)
            .show()
    }

    private fun handlePermissionDenied(permission: String) {
        showNonCancelablePermissionDeniedDialog(permission)
        callback?.onLocationPermissionDenied()
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
            }
            settingsLauncher.launch(intent) // Use settingsLauncher to detect return
        } catch (_: ActivityNotFoundException) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS)
                settingsLauncher.launch(intent)
            } catch (_: Exception) {
                // Ignore
            }
        }
    }
}