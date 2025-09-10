package com.sap.codelab.create.presentation.components

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

fun interface OnLocationSetListener {
    fun onLocationSet(latitude: Double, longitude: Double)
}

@Composable
fun SelectableMap(
    modifier: Modifier = Modifier,
    onLocationSetListener: OnLocationSetListener,
    sharedPreferences: SharedPreferences = koinInject()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Read last saved location from SharedPreferences
    val lastLat = sharedPreferences.getFloat("LAST_LATITUDE", 52.5101f) // SAP Berlin fallback
    val lastLng = sharedPreferences.getFloat("LAST_LONGITUDE", 13.4399f)

    val cameraPositionState = remember {
        CameraPositionState(
            position = CameraPosition.fromLatLngZoom(
                LatLng(lastLat.toDouble(), lastLng.toDouble()),
                10f
            )
        )
    }

    var mapReady by remember { mutableStateOf(false) }
    val markerState = remember { MarkerState(LatLng(0.0, 0.0)) }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(zoomControlsEnabled = true),
            onMapClick = { latLng ->
                markerState.position = latLng
                scope.launch {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                }
                onLocationSetListener.onLocationSet(latLng.latitude, latLng.longitude)
            },
            onMapLoaded = { mapReady = true }
        ) {
            // Only show marker if user clicked
            if (markerState.position.latitude != 0.0 && markerState.position.longitude != 0.0) {
                Marker(state = markerState, title = "Selected Location")
            }
        }

        // Animate camera to last known location / current location
        LaunchedEffect(mapReady) {
            if (mapReady &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                            )
                        }
                    }
                }
            }
        }
    }
}