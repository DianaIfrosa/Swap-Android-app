package com.diana.bachelorthesis.view

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.diana.bachelorthesis.databinding.FragmentMapBinding
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.utils.LocationHelper
import com.diana.bachelorthesis.viewmodel.ItemsViewModel
import com.diana.bachelorthesis.viewmodel.MapViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.utils.BasicFragment
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.MapsInitializer.Renderer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.libraries.places.api.Places
import java.util.*

class MapFragment : Fragment(), BasicFragment, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, OnMapsSdkInitializedCallback {

    private val TAG: String = MapFragment::class.java.name
    companion object {
        private var LOCATION_PERMISSION_REQUEST: Int = 50
    }
    private var _binding: FragmentMapBinding? = null

    private val binding get() = _binding!!
    private lateinit var googleMap : GoogleMap
    private var lastLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var mapLoaded: Boolean = false
    private var locationPermissionAccepted: Boolean = false

    private lateinit var mapViewModel: MapViewModel
    private lateinit var itemsViewModel: ItemsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MapFragment is onCreate")
       }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "MapFragment is onCreateView")
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.mapFragment.visibility = View.GONE
        binding.placeAutocompleteFragment.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "MapFragment is onViewCreated")
        MapsInitializer.initialize(requireActivity().applicationContext, Renderer.LATEST, this)

        if (!Places.isInitialized()) {
            Places.initialize(requireActivity().applicationContext, getString(R.string.api_key))
        }

        getViewModels()
        initListeners()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "MapFragment is onActivityCreated")
        setMainPageAppbar(requireActivity(), requireView().findNavController().currentDestination!!.label.toString())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        mapFragment.getMapAsync(this)
    }

    private fun getViewModels() {
        mapViewModel = ViewModelProvider(this)[MapViewModel::class.java]
        val viewModelFactory = ItemsViewModel.ViewModelFactory(LocationHelper(requireActivity().applicationContext))
        itemsViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ItemsViewModel::class.java]
    }

    override fun onMapsSdkInitialized(renderer: Renderer) {
        when (renderer) {
            Renderer.LATEST -> Log.d(TAG, "The latest version of the renderer is used.")
            Renderer.LEGACY -> Log.d(TAG, "The legacy version of the renderer is used.")
        }
    }

     private fun initMapPlaces() {

        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as AutocompleteSupportFragment

         // specify the types of place data to return
         autocompleteFragment.setPlaceFields(listOf(Place.Field.LAT_LNG, Place.Field.NAME))

         autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
             override fun onPlaceSelected(place: Place) {
                 place.latLng?.let { latLng ->
                     googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
                 }
                 Log.i(TAG, "Place selected: ${place.name}, ${place.latLng}")
             }

             override fun onError(status: Status) {
                 Log.i(TAG, "An error occurred (or the user didn't type anything): $status")
             }
         })
     }

    private fun getAllItems() {
        if (itemsViewModel.donationItems.value != null) {
            mapViewModel.updateItems(false, itemsViewModel.donationItems.value!!)
        }

        if (itemsViewModel.exchangeItems.value != null) {
            mapViewModel.updateItems(true, itemsViewModel.exchangeItems.value!!)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        mapLoaded = true

        binding.progressBar.visibility = View.GONE
        binding.mapFragment.visibility = View.VISIBLE
        binding.placeAutocompleteFragment.visibility = View.VISIBLE

        initMapPlaces()
        getAllItems()
        updateMapMarkers()

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setOnMarkerClickListener(this)
        setupMap()
    }

    private fun requestLocationPermission() {

            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                // We have to show an explanation to the user
                AlertDialog.Builder(requireActivity())
                    .setTitle(resources.getString(R.string.titleLocationPermission))
                    .setMessage(resources.getString(R.string.messageLocationPermission))
                    .setPositiveButton("OK") { _, _ ->
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            LOCATION_PERMISSION_REQUEST)
                    }
                    .create()
                    .show()
            } else {
                // We don't have to show any explanation
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST)
            }

    }

    private fun setupMap() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()

        } else {
        googleMap.isMyLocationEnabled = true
        googleMap.setOnMyLocationButtonClickListener { false }

        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            if (location != null) {
                lastLocation = location
                Log.d(tag, "last location is $location")
                markerCurrentLocation(lastLocation!!)
                val currentLatLng = LatLng(location.latitude, location.longitude)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13f))

            }
        }
        }
    }

    private fun markerCurrentLocation(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions().position(currentLatLng).title(getString(R.string.you)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        googleMap.addMarker(markerOptions)
    }

    override fun initListeners() {
        itemsViewModel.donationItems.observe(viewLifecycleOwner) { items ->
            mapViewModel.updateItems(false, items)
            updateMapMarkers()
        }

        itemsViewModel.exchangeItems.observe(viewLifecycleOwner) { items ->
            mapViewModel.updateItems(true, items)
            updateMapMarkers()
        }
    }

    private fun updateMapMarkers() {
        if (mapLoaded) {
            googleMap.clear() // TODO make sure the current location marker is added again
            lastLocation?.let { markerCurrentLocation(it) }
            mapViewModel.allItems.forEach { item ->
                val marker = LatLng(item.location.latitude, item.location.longitude)
                if (item is ItemExchange) {
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(marker)
                            .title(item.name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                    )
                } else {
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(marker)
                            .title(item.name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    )
                }
            }
        }
    }

//    private fun displayCurrentLocation() {
//
//        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
//            !=  PackageManager.PERMISSION_GRANTED
//            && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED) {
//
//            requestMultiplePermissions.launch(arrayOf(
//                Manifest.permission.ACESS_FINE_LOCATION,
//                Manifest.permission.ACESS_COARSE_LOCATION))
//            return
//        }
//
//        googleMap.isMyLocationEnabled = true
//
//        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//            if (location != null) {
//                lastLocation = location
//                val currentLatLng = LatLng(location.latitude, location.longitude)
//                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
//            }
//        }
//    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        setupMap()
                    }
                }
                return
            }
        }
    }

//    override fun onStop() {
//        super.onStop()
//        Log.d(TAG, "on stop")
//        // when fragment is not visible stop location updates
//        fusedLocationClient.removeLocationUpdates(locationCallback)
//    }

    override fun onMarkerClick(marker: Marker) = false

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "MapFragment is onDestroyView")
        _binding = null
    }
}