package com.diana.bachelorthesis.view

import android.content.pm.PackageManager
import android.location.Location
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentMapBinding
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.utils.LocationHelper
import com.diana.bachelorthesis.viewmodel.ItemsViewModel
import com.diana.bachelorthesis.viewmodel.MapViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment() {

    private val TAG: String = MapFragment::class.java.name
    private var LOCATION_PERMISSION_REQUEST: Int = 50
    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var googleMap : GoogleMap
    var mapFragment: SupportMapFragment? = null
    lateinit var locationRequest: LocationRequest
    private var currentLocationMarker: Marker? = null

    private lateinit var lastLocation : Location
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private var mapLoaded: Boolean = false

    private var locationCallback: LocationCallback = object: LocationCallback() {
        override fun onLocationResult(locRes: LocationResult) {
            val locList = locRes.locations
            if (locList.isNotEmpty()) {
                lastLocation = locList.last()
                currentLocationMarker?.remove() // the last location saved has changed

                // update the current location marker
                val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                val markerOptions = MarkerOptions().position(latLng).title("Current").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                currentLocationMarker = googleMap.addMarker(markerOptions)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
            }
        }
    }

    private lateinit var mapViewModel: MapViewModel
    private lateinit var itemsViewModel: ItemsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "MapFragment is onCreateView")

        mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        val viewModelFactory = ItemsViewModel.ViewModelFactory(LocationHelper(requireActivity().applicationContext))
        itemsViewModel = ViewModelProvider(this, viewModelFactory).get(ItemsViewModel::class.java)

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?

        mapFragment?.getMapAsync {
            map ->
            googleMap = map
            mapLoaded = true
            setupMap()
            updateMapMarkers()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        initListeners()

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "MapFragment is onActivityCreated")
        setAppbar()
    }

    private fun setupMap() {
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true

        locationRequest = LocationRequest()
        locationRequest.priority= LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = 180000 // 3 minutes
        locationRequest.fastestInterval = 180000
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // location permission granted
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                googleMap.isMyLocationEnabled = true
            } else {
                // request location permission
                requestLocationPermission()
            }
        } else {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            googleMap.isMyLocationEnabled = true
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
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
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                        googleMap.isMyLocationEnabled = true
                    }
                }
            }
        }
    }


    private fun initListeners() {
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
            // googleMap.clear()
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

    private fun setAppbar() {
        activity?.findViewById<TextView>(R.id.titleAppBar)?.apply {
            visibility = View.VISIBLE
            text = context.getString(R.string.mapPageTitle)
        }
        activity?.findViewById<ImageView>(R.id.logoApp)?.apply {
            visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        // when fragment is not visible stop location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "MapFragment is onDestroyView")
        _binding = null
    }

}