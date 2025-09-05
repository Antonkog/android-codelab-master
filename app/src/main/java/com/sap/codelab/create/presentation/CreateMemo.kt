package com.sap.codelab.create.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityCreateMemoBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity that allows a user to create a new Memo.
 */
internal class CreateMemo : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCreateMemoBinding
    private val viewModel: CreateMemoViewModel by viewModel()
    private lateinit var map: GoogleMap
    private var selectedLatLng: LatLng? = null

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
        // Optional: Set default location (e.g., city center or user’s last known location)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 1f))

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
            selectedLatLng = latLng
            map.clear()  // Clear previous markers
            map.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
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
        viewModel.saveMemo()
        setResult(RESULT_OK)
        finish()
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
                    // Handle unexpected denial
                }
            }
        }
}