package com.diana.bachelorthesis.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment

import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentLocationDialogBinding
import com.diana.bachelorthesis.utils.LocationDialogListener
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.firestore.GeoPoint

class LocationDialogFragment : DialogFragment(), OnMapReadyCallback,
    OnMapsSdkInitializedCallback {
    private val TAG: String = LocationDialogFragment::class.java.name

    private var _binding: FragmentLocationDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var fragmentParent: AddItemFragment
    private lateinit var toolbar: Toolbar
    private lateinit var googleMap: GoogleMap
    private var mapLoaded: Boolean = false
    private var locationChosen: Place? = null
    private var previousLocationChosen: GeoPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "ItemLocationFragment is onCreate")
        setStyle(STYLE_NORMAL,  R.style.FullScreenDialog)

        val bundle = this.arguments
        val lat = bundle?.getDouble("latitude") ?: 0.0
        val long = bundle?.getDouble("longitude") ?: 0.0
        previousLocationChosen = GeoPoint(lat, long)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "ItemLocationFragment is onCreateView")
        _binding = FragmentLocationDialogBinding.inflate(inflater, container, false)
        val root: View = binding.root
        fragmentParent = parentFragment as AddItemFragment
//        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        toolbar = root.findViewById(R.id.toolbar_dialog_location)
        binding.mapFragment.visibility = View.GONE
        binding.placeAutocompleteFragment.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        return root
    }


//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return super.onCreateDialog(savedInstanceState)
//    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "ItemLocationFragment is onViewCreated")
        toolbar.title = getString(R.string.choose_location)
        toolbar.setTitleTextColor(resources.getColor(R.color.purple_dark))
        toolbar.setNavigationOnClickListener {
            dialog!!.dismiss()
        }
        toolbar.inflateMenu(R.menu.choose_location_menu)
        toolbar.setOnMenuItemClickListener {
            val listener: LocationDialogListener = fragmentParent
            listener.saveLocation(locationChosen)
            dialog!!.dismiss()
            true
        }
        initMaps()
//        initListeners()
    }

    private fun initMaps() {
        MapsInitializer.initialize(
            requireActivity().applicationContext,
            MapsInitializer.Renderer.LATEST,
            this
        )

        if (!Places.isInitialized()) {
            Places.initialize(requireActivity().applicationContext, getString(R.string.api_key))
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog!!.window?.setLayout(width, height)
        }
    }

    private fun initListeners() {
//        binding.toolbarDialogLocation.setOnClickListener {
//            val listener: LocationDialogListener = fragmentParent
//            listener.saveLocation(locationChosen)
//            dialog!!.dismiss()
//        }

        binding.toolbarDialogLocation.setOnClickListener {
            dialog!!.dismiss()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        mapLoaded = true

        binding.progressBar.visibility = View.GONE
        binding.mapFragment.visibility = View.VISIBLE
        binding.placeAutocompleteFragment.visibility = View.VISIBLE

        displayPreviousLocation()
        initMapPlaces()
    }

    private fun displayPreviousLocation() {
        if (previousLocationChosen != null &&
            previousLocationChosen!!.latitude != 0.0 &&
            previousLocationChosen!!.longitude != 0.0
        ) {

            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        previousLocationChosen!!.latitude,
                        previousLocationChosen!!.longitude
                    ), 13f
                )
            )
            val markerOptions = MarkerOptions().position(LatLng(previousLocationChosen!!.latitude, previousLocationChosen!!.longitude)).title(getString(R.string.location_chosen)).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            googleMap.addMarker(markerOptions)
        }
    }

    private fun initMapPlaces() {

        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as AutocompleteSupportFragment

        // specify the types of place data to return
        autocompleteFragment.setPlaceFields(listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS))
        autocompleteFragment.setHint(getString(R.string.search))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                autocompleteFragment.setHint(place.address)
                place.latLng?.let { latLng ->
                    googleMap.clear()
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
                    val markerOptions = MarkerOptions().position(LatLng(latLng.latitude, latLng.longitude)).title(getString(R.string.location_chosen)).icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    googleMap.addMarker(markerOptions)
                }
                Log.i(TAG, "Place selected: ${place.name}, ${place.latLng}")

                locationChosen = place
            }

            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred (or the user didn't type anything): $status")
            }
        })
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> Log.d(
                TAG,
                "The latest version of the renderer is used."
            )
            MapsInitializer.Renderer.LEGACY -> Log.d(
                TAG,
                "The legacy version of the renderer is used."
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "ItemLocationFragment is onDestroyView")
        _binding = null

    }

}