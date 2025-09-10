package com.sap.codelab.loading.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsLoadingScreen(
    onAllPermissionsGranted: () -> Unit
) {

    val context = LocalContext.current

    val locationPermissionState =
        rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val backgroundLocationPermissionState =
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    val notificationPermissionState =
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)

    val allGranted = locationPermissionState.status.isGranted &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || backgroundLocationPermissionState.status.isGranted) &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || notificationPermissionState.status.isGranted)

    // Trigger callback when all permissions granted
    LaunchedEffect(allGranted) {
        if (allGranted) onAllPermissionsGranted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("We need permissions to work properly", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))

        PermissionProgressItem(
            title = "Location",
            description = "Allow access to your location",
            granted = locationPermissionState.status.isGranted
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PermissionProgressItem(
                title = "Background Location",
                description = "Allow location access in the background",
                granted = backgroundLocationPermissionState.status.isGranted
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionProgressItem(
                title = "Notifications",
                description = "Allow notifications",
                granted = notificationPermissionState.status.isGranted
            )
        }

        Spacer(Modifier.height(24.dp))

        var askedOnce by remember { mutableStateOf(false) }

        Button(onClick = {
            when {
                !locationPermissionState.status.isGranted -> {
                    if (!askedOnce || locationPermissionState.status.shouldShowRationale) {
                        askedOnce = true
                        locationPermissionState.launchPermissionRequest()
                    } else {
                        openAppSettings(context)
                    }
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                        !backgroundLocationPermissionState.status.isGranted -> {
                    if (!askedOnce || backgroundLocationPermissionState.status.shouldShowRationale) {
                        askedOnce = true
                        backgroundLocationPermissionState.launchPermissionRequest()
                    } else {
                        openAppSettings(context)
                    }
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !notificationPermissionState.status.isGranted -> {
                    if (!askedOnce || notificationPermissionState.status.shouldShowRationale) {
                        askedOnce = true
                        notificationPermissionState.launchPermissionRequest()
                    } else {
                        openAppSettings(context)
                    }
                }
            }
        }) {
            Text("Grant Permissions")
        }
    }
}

@Composable
fun PermissionProgressItem(title: String, description: String, granted: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = null,
            tint = if (granted) Color.Green else Color.Gray
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}
