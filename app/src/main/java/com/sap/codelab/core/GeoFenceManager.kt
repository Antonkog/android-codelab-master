package com.sap.codelab.core

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.sap.codelab.core.domain.Memo
import com.sap.codelab.utils.Constants


class GeoFenceManager(
    private val context: Context,
    private val geofencingClient: GeofencingClient
) {


    fun addGeofence(memo: Memo) {
        val latitude = memo.reminderLatitude
        val longitude = memo.reminderLongitude
        val geofence = Geofence.Builder()
            .setRequestId(memo.id.toString())
            .setCircularRegion(
                latitude,
                longitude,
                Constants.GEOFENCE_RADIUS_IN_METERS
            ) // 500m radius
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        }
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener {
                    Log.d(
                        "GeoFenceManager",
                        "Geofence added successfully $geofence + requestID ${geofence.requestId}lat: $latitude lng: $longitude"
                    )
                }.addOnFailureListener { e ->
                Log.d(
                    "GeoFenceManager",
                    "Failed to add geofence: ${e.message} +  $geofence + requestID ${geofence.requestId}lat: $latitude lng: $longitude"
                )
            }
        }
    }
}