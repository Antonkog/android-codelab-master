package com.sap.codelab.core.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sap.codelab.R
import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.core.domain.Memo
import com.sap.codelab.main.MainActivity
import com.sap.codelab.utils.Constants
import com.sap.codelab.utils.Constants.BUNDLE_MEMO_ID
import com.sap.codelab.utils.Constants.LAST_LATITUDE
import com.sap.codelab.utils.Constants.LAST_LONGITUDE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class LocationService : Service() {

    private val repo: IMemoRepository by inject()
    private val sharedPreferences: SharedPreferences by inject()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val serviceSupervisor = SupervisorJob()
    private val serviceScope: CoroutineScope = CoroutineScope(serviceSupervisor + Dispatchers.IO)
    private var serviceJob: Job? = null
    private var memos: List<Memo> = emptyList()

    companion object {
        @Volatile
        var instance: LocationService? = null
        private const val TAG = "LocationService"
        private const val FOREGROUND_ID = 127
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "location_channel"
        private const val CHANNEL_NAME = "Location Services"
        private const val UPDATE_INTERVAL = 5000L // 5 seconds

        @Volatile
        private var running: Boolean = false
        fun isRunning(): Boolean = running
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { currentLocation ->
                    sharedPreferences.edit().apply {
                        putFloat(LAST_LATITUDE, currentLocation.latitude.toFloat())
                        putFloat(LAST_LONGITUDE, currentLocation.longitude.toFloat())
                        apply()
                    }
                    if (memos.isNotEmpty()) {
                        memos.forEach { memo ->
                            val target = Location("target").apply {
                                latitude = memo.reminderLatitude
                                longitude = memo.reminderLongitude
                            }
                            val distance = currentLocation.distanceTo(target)
                            Log.d(TAG, "Distance to memo ${memo.id}: $distance meters")
                            if (distance <= Constants.NOTIFICATION_RADIUS_IN_METERS) {
                                showMemoNotification(memo)
                                // Mark as notified in DB to avoid duplicates
                                serviceScope.launch {
                                    try {
                                        repo.saveMemo(memo.copy(isNotificationShown = true))
                                    } catch (e: Exception) {
                                        Log.e(
                                            TAG,
                                            "Failed to mark memo as notified: ${e.message}",
                                            e
                                        )
                                    }
                                }
                            }
                        }
                    }
                } ?: Log.w(TAG, "Current location is null")
            }
        }
        Log.w(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start background work without forcing foreground notification.
        // Foreground promotion will be controlled by App lifecycle callbacks.
        if (serviceJob == null) {
            running = true
            serviceJob = serviceScope.launch {
                try {
                    // Observe open memos continuously
                    repo.getNotNotifiedMemosAsFlow().collect { list ->
                        memos = list
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error observing memos: ${e.message}", e)
                }
            }
            startLocationUpdates()
        }
        return START_STICKY
    }

    fun promoteToForeground() {
        createNotificationChannel()
        val foregroundNotification = buildForegroundNotification()
        ServiceCompat.startForeground(
            this,
            FOREGROUND_ID,
            foregroundNotification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            } else {
                0
            }
        )
    }

    fun demoteFromForeground(removeNotification: Boolean = true) {
        // Stop being a foreground service but keep it running
        try {
            ServiceCompat.stopForeground(
                this,
                if (removeNotification) ServiceCompat.STOP_FOREGROUND_REMOVE else 0
            )
        } catch (e: Exception) {
            Log.w(TAG, "stopForeground failed: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.location_service_channel)
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.location_service_on_title))
            .setContentText(getString(R.string.location_service_on))
            .setSmallIcon(R.drawable.ic_search_location)
            .setLargeIcon(
                IconCompat.createWithResource(this, R.drawable.ic_search_location).toIcon(this)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setSound(null)
            .build()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(UPDATE_INTERVAL)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(UPDATE_INTERVAL)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
            Log.d(TAG, "Requesting location updates")
        } catch (e: SecurityException) {
            Log.e(TAG, "Error requesting location updates: ${e.message}", e)
            stopSelf()
        }
    }

    private fun stopLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping location updates: ${e.message}", e)
        }
    }

    private fun showMemoNotification(memo: Memo) {
        val text = memo.description.take(Constants.NOTIFICATION_CHARS_COUNT)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(BUNDLE_MEMO_ID, memo.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(memo.title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_note)
            .setLargeIcon(IconCompat.createWithResource(this, R.drawable.ic_note).toIcon(this))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            NOTIFICATION_ID,
            notification
        )//optional  + memo.id.toInt() but i want to replace previous notification
        Log.d(TAG, "Notification shown for memo ID: ${memo.id}")
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        running = false
        instance = null
        serviceJob?.cancel()
        // Cancel the entire service scope to stop any ongoing work tied to this service
        serviceSupervisor.cancel()
        stopLocationUpdates()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}