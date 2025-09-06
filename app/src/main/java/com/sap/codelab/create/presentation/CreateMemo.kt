package com.sap.codelab.create.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.sap.codelab.R
import com.sap.codelab.core.presentation.LocationService
import com.sap.codelab.databinding.ActivityCreateMemoBinding
import com.sap.codelab.utils.Constants
import com.sap.codelab.utils.Constants.LAST_LATITUDE
import com.sap.codelab.utils.Constants.LAST_LONGITUDE
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity that allows a user to create a new Memo.
 */
internal class CreateMemo : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCreateMemoBinding
    private val viewModel: CreateMemoViewModel by viewModel()
    private lateinit var map: GoogleMap
    private val fusedLocationClient: FusedLocationProviderClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Initialize map
        binding.contentCreateMemo.map.onCreate(savedInstanceState)
        binding.contentCreateMemo.map.getMapAsync(this)

        // Request location permission for My Location button
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (!isLocationEnabled(baseContext)) {
            Log.e("GeoFenceManager", "Location services are disabled")
            Toast.makeText(baseContext, "Please enable location services", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        if (!isGooglePlayServicesAvailable(baseContext)) {
            Log.e("GeoFenceManager", "Google Play Services not available")
            Toast.makeText(baseContext, "Google Play Services required for geofencing", Toast.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()
        binding.contentCreateMemo.map.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.contentCreateMemo.map.onResume()
    }

    override fun onPause() {
        binding.contentCreateMemo.map.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.contentCreateMemo.map.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        binding.contentCreateMemo.map.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.contentCreateMemo.map.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.contentCreateMemo.map.onLowMemory()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location?.latitude != null)
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location.latitude,
                            location.longitude
                        ), 15f
                    )
                )
        }.addOnFailureListener { e ->
            val sharedPreferences: SharedPreferences by inject()
            val lat = sharedPreferences.getFloat(LAST_LATITUDE, 0F)
            val lng = sharedPreferences.getFloat(LAST_LONGITUDE, 0F)
            if (lat != 0F && lng != 0F)
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(lat.toDouble(), lng.toDouble()),
                        15f
                    )
                )
        }

        // Enable My Location if permission granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        }

        // Set click listener for map
        map.setOnMapClickListener { latLng ->
            map.clear()  // Clear previous markers
            map.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            // Update ViewModel with coordinates
            viewModel.updateLocation(latLng.latitude, latLng.longitude)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_memo, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveMemo()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveMemo() = with(binding.contentCreateMemo) {
        viewModel.updateMemo(memoTitle.text.toString(), memoDescription.text.toString())

        if (viewModel.isMemoValid()) {
            handleValidMemo()
        } else {
            showValidationErrors()
        }
    }

    private fun handleValidMemo() {
        lifecycleScope.launch {
            viewModel.saveMemo()
            viewModel.addGeofence()
            setResult(RESULT_OK)
            finish()
        }
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }
    private fun showValidationErrors() = with(binding.contentCreateMemo) {
        memoTitleContainer.error =
            getErrorMessage(viewModel.hasTitleError(), R.string.memo_title_empty_error)
        memoDescriptionContainer.error =
            getErrorMessage(viewModel.hasTextError(), R.string.memo_text_empty_error)
        // Optional: Add location error if no location selected
        if (!viewModel.hasValidLocation()) {
            Toast.makeText(
                this@CreateMemo,
                "Please select a location on the map",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getErrorMessage(hasError: Boolean, @StringRes errorMessageResId: Int): String? =
        if (hasError) getString(errorMessageResId) else null

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted && ::map.isInitialized) {
                try {
                    map.isMyLocationEnabled = true
                } catch (e: SecurityException) {
                    Log.e("CreateMemo", "Error enabling location: ${e.message}")
                    // Handle unexpected denial
                }
            }
        }

    private fun startForegroundService(memoId: Long) {
        val serviceIntent = Intent(applicationContext, LocationService::class.java).apply {
            putExtra(Constants.BUNDLE_MEMO_ID, memoId)
        }
        startForegroundService(serviceIntent)
    }
}