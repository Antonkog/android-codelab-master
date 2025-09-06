package com.sap.codelab.core
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.sap.codelab.core.presentation.LocationService
import com.sap.codelab.utils.Constants
import kotlin.jvm.java

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent?.hasError() == true) {
            Log.d("GeofenceBroadcastReceiver", "geofencingEvent.hasError(): $geofencingEvent")
            return
        }

        if (geofencingEvent?.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            geofencingEvent.triggeringGeofences?.forEach { geofence ->
                val memoId = geofence.requestId
                // Start foreground service to confirm location
                val serviceIntent = Intent(context, LocationService::class.java).apply {
                    putExtra(Constants.BUNDLE_MEMO_ID, memoId)
                }
                Log.d("GeofenceBroadcastReceiver", "Starting Location service")
                context.startForegroundService(serviceIntent)
            }
        }
    }
}