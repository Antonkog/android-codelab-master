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
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import com.sap.codelab.databinding.FragmentCreateMemoBinding
import com.sap.codelab.utils.Constants.LAST_LATITUDE
import com.sap.codelab.utils.Constants.LAST_LONGITUDE
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateMemoFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentCreateMemoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateMemoViewModel by viewModel()
    private lateinit var map: GoogleMap
    private val fusedLocationClient: FusedLocationProviderClient by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateMemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        binding.map.onCreate(savedInstanceState)
        binding.map.getMapAsync(this)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (!isLocationEnabled(requireContext())) {
            Log.e("GeoFenceManager", "Location services are disabled")
            Toast.makeText(requireContext(), "Please enable location services", Toast.LENGTH_LONG)
                .show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        if (!isGooglePlayServicesAvailable(requireContext())) {
            Log.e("GeoFenceManager", "Google Play Services not available")
            Toast.makeText(
                requireContext(),
                "Google Play Services required for geofencing",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onStart() {
        super.onStart()
        binding.map.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        binding.map.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.map.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        binding.map.onDestroy()
        _binding = null
        super.onDestroyView()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.map.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.map.onSaveInstanceState(outState)
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

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        }

        map.setOnMapClickListener { latLng ->
            map.clear()
            map.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            viewModel.updateLocation(latLng.latitude, latLng.longitude)
        }
    }

    private fun setupMenu() {
        val menuHost: androidx.core.view.MenuHost = requireActivity()
        menuHost.addMenuProvider(object : androidx.core.view.MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_create_memo, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_save -> {
                        saveMemo()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner)
    }

    private fun saveMemo() = with(binding) {
        viewModel.updateMemo(memoTitle.text.toString(), memoDescription.text.toString())

        if (viewModel.isMemoValid()) {
            handleValidMemo()
        } else {
            showValidationErrors()
        }
    }

    private fun handleValidMemo() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveMemo()
            viewModel.addGeofence()
            requireActivity().onBackPressedDispatcher.onBackPressed()
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

    private fun showValidationErrors() = with(binding) {
        memoTitleContainer.error =
            getErrorMessage(viewModel.hasTitleError(), R.string.memo_title_empty_error)
        memoDescriptionContainer.error =
            getErrorMessage(viewModel.hasTextError(), R.string.memo_text_empty_error)
        if (!viewModel.hasValidLocation()) {
            Toast.makeText(
                requireContext(),
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
                }
            }
        }
}
