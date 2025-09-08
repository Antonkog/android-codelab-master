package com.sap.codelab.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.sap.codelab.R
import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.core.domain.Memo
import com.sap.codelab.core.presentation.LocationService
import com.sap.codelab.main.MainActivity
import com.sap.codelab.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent?.hasError() == true) {
            Log.d("GeofenceBroadcastReceiver", "geofencingEvent.hasError(): $geofencingEvent")
            return
        }

        if (geofencingEvent?.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            geofencingEvent.triggeringGeofences?.forEach { geofence ->
                val memoId = geofence.requestId.toLongOrNull()
                if (memoId != null) {
                    if (LocationService.isRunning()) {
                        Log.d(
                            "GeofenceBroadcastReceiver",
                            "LocationService is running; ignoring geofence trigger"
                        )
                        return
                    } else {
                        Log.d(
                            "GeofenceBroadcastReceiver",
                            "Service not running; showing memo notification directly"
                        )
                        showMemoNotificationFallback(context, memoId)
                    }
                }
            }
        }
    }

    private fun showMemoNotificationFallback(context: Context, memoId: Long) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo: IMemoRepository = KoinJavaComponent.get(IMemoRepository::class.java)
                val memo: Memo? = repo.getMemoById(memoId)
                if (memo != null) {
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    // Ensure channel exists
                    val channelId = "location_channel"
                    val channel = NotificationChannel(
                        channelId,
                        "Location Services",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationManager.createNotificationChannel(channel)

                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(Constants.BUNDLE_MEMO_ID, memo.id)
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val text = memo.description.take(Constants.NOTIFICATION_CHARS_COUNT)
                    val notification =
                        androidx.core.app.NotificationCompat.Builder(context, channelId)
                            .setContentTitle(memo.title)
                            .setContentText(text)
                            .setSmallIcon(R.drawable.ic_note)
                            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent)
                            .build()
                    notificationManager.notify(1002 + memo.id.toInt(), notification)
                    // Mark memo as notified in DB to prevent duplicates
                    try {
                        repo.saveMemo(memo.copy(isNotificationShown = true))
                    } catch (_: Exception) {
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    "GeofenceBroadcastReceiver",
                    "Failed to show fallback notification: ${e.message}",
                    e
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}