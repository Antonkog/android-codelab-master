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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sap.codelab.R
import com.sap.codelab.core.domain.IMemoRepository
import com.sap.codelab.core.domain.Memo
import com.sap.codelab.home.presentation.Home
import com.sap.codelab.utils.Constants
import com.sap.codelab.utils.Constants.BUNDLE_MEMO_ID
import com.sap.codelab.utils.Constants.LAST_LATITUDE
import com.sap.codelab.utils.Constants.LAST_LONGITUDE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LocationService : Service() {

    private val repo: IMemoRepository by inject()
    private val sharedPreferences: SharedPreferences by inject()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var serviceJob: Job? = null
    private var memo: Memo? = null
    private var targetLocation: Location? = null

    companion object {
        private const val TAG = "LocationService"
        private const val FOREGROUND_ID = 127
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "location_channel"
        private const val CHANNEL_NAME = "Location Services"
        private const val UPDATE_INTERVAL = 5000L // 5 seconds
        private const val TIMEOUT_DURATION = 600000L // 10 minutes timeout
    }

    override fun onCreate() {
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
                    targetLocation?.let { target ->
                        val distance = currentLocation.distanceTo(target)
                        Log.d(TAG, "Distance to target: $distance meters")
                        if (distance <= Constants.NOTIFICATION_RADIUS_IN_METERS) {
                            showMemoNotification()
                            stopLocationUpdates()
                            stopSelf()
                        }
                    } ?: Log.w(TAG, "Target location is null")
                } ?: Log.w(TAG, "Current location is null")
            }
        }
        Log.w(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val memoId = intent?.getStringExtra(BUNDLE_MEMO_ID)?.toLongOrNull()
        if (memoId == null) {
            Log.e(TAG, "Invalid memoId, stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        // Start foreground service immediately
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

        // Perform async work after starting foreground
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                memo = repo.getMemoById(memoId)
                memo?.let {
                    targetLocation = Location("target").apply {
                        latitude = it.reminderLatitude
                        longitude = it.reminderLongitude
                    }
                    startLocationUpdates()
                } ?: run {
                    Log.e(TAG, "Memo not found for ID: $memoId")
                    stopSelf()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching memo: ${e.message}", e)
                stopSelf()
            }
        }

        return START_STICKY
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
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSound(null)
            .build()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(UPDATE_INTERVAL)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(UPDATE_INTERVAL)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
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

    private fun showMemoNotification() {
        memo?.let { m ->
            val text = m.description.take(Constants.NOTIFICATION_CHARS_COUNT)
            val intent = Intent(this, Home::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(BUNDLE_MEMO_ID, m.id)
            }
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(m.title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_note)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Notification shown for memo ID: ${m.id}")
        } ?: Log.w(TAG, "Memo is null, cannot show notification")
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        serviceJob?.cancel()
        stopLocationUpdates()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}