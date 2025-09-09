package com.sap.codelab.utils.permissions

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsLoadingScreen(
    onAllPermissionsGranted: () -> Unit
) {
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
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

        Button(onClick = {
            when {
                !locationPermissionState.status.isGranted -> locationPermissionState.launchPermissionRequest()
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !backgroundLocationPermissionState.status.isGranted ->
                    backgroundLocationPermissionState.launchPermissionRequest()

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionState.status.isGranted ->
                    notificationPermissionState.launchPermissionRequest()
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
