package com.diana.bachelorthesis.view

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources.NotFoundException
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.FrameLayout.LayoutParams
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apachat.loadingbutton.core.customViews.CircularProgressButton
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.PhotosRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentAddItemBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.model.ItemCondition
import com.diana.bachelorthesis.utils.*
import com.diana.bachelorthesis.viewmodel.AddItemViewModel
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.GeoPoint
import java.util.*
import kotlin.collections.ArrayList

class AddItemFragment : Fragment(), AdapterView.OnItemSelectedListener, BasicFragment,
    LocationDialogListener {

    private val TAG: String = AddItemFragment::class.java.name
    private val MIN_LENGTH_TITLE = 3
    private val MIN_LENGTH_DESCRIPTION = 5
    private val MIN_PHOTOS = 2
    private val PICK_IMAGE_CODE = 10
    private var shouldCleanUI = true

    private lateinit var addItemViewModel: AddItemViewModel
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var _binding: FragmentAddItemBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "AddItemFragment is onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "AddItemFragment is onCreateView")

        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val horizontalLayoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.photosRecyclerView.layoutManager = horizontalLayoutManager
        updatePhotosRecyclerView(arrayListOf())
        getViewModels()

        initListeners()
        attachCategoryAdapter()
        attachConditionAdapter()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "AddItemFragment is onViewCreated")
        setMainPageAppbar(
            requireActivity(),
            requireView().findNavController().currentDestination!!.label.toString()
        )
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "AddItemFragment is onStart")

        if (shouldCleanUI)
            cleanUIElements()
    }

    private fun getViewModels() {
        addItemViewModel = ViewModelProvider(this)[AddItemViewModel::class.java]
    }

    // TODO make a general function for spinner adapter that returns the adapter and receives the arrayList of elements
    private fun attachCategoryAdapter() {
        val spinnerCategories = binding.spinnerCategories
        spinnerCategories.onItemSelectedListener = this
        spinnerCategories.setSelection(0)
        val categories = ItemCategory.values().map { it.displayName } as MutableList
        categories.add(0, getString(R.string.select)) // hint

        try {
            val lastItem = categories.removeLast()
            if (lastItem != ItemCategory.UNKNOWN.displayName)
                throw NotFoundException("UNKNOWN category is not last")
        } catch (ex: Exception) {
            Log.w(TAG, ex.stackTraceToString())
        }

        val categoriesAdapter = object : ArrayAdapter<String>(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            categories
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                // make first option grey, as a hint
                if (position == 0)
                    view.setTextColor(ContextCompat.getColor(requireActivity(), R.color.grey))
                return view
            }
        }
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerCategories.adapter = categoriesAdapter
    }

    private fun attachConditionAdapter() {
        val spinnerCondition = binding.spinnerCondition
        spinnerCondition.onItemSelectedListener = this
        spinnerCondition.setSelection(0)
        val conditionArray = ItemCondition.values().map { it.displayName } as MutableList
        conditionArray.add(0, getString(R.string.select)) // hint

        try {
            val lastItem = conditionArray.removeLast()
            if (lastItem != ItemCondition.UNKNOWN.displayName)
                throw NotFoundException("UNKNOWN condition is not last")
        } catch (ex: Exception) {
            Log.w(TAG, ex.stackTraceToString())
        }

        val conditionAdapter = object : ArrayAdapter<String>(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            conditionArray
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                // make first option grey, as a hint
                if (position == 0)
                    view.setTextColor(ContextCompat.getColor(requireActivity(), R.color.grey))
                return view
            }
        }
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerCondition.adapter = conditionAdapter
    }

    override fun initListeners() {
        binding.textOtherDetails.setOnClickListener {
            if (binding.hiddenLayoutOtherDetails.visibility == View.GONE) {
                (it as TextView).setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_arrow_dropup,
                    0
                )
                it.setTag(R.drawable.ic_arrow_dropup)
                binding.hiddenLayoutOtherDetails.visibility = View.VISIBLE
            } else {
                (it as TextView).setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_arrow_dropdown,
                    0
                )
                it.setTag(R.drawable.ic_arrow_dropdown)
                binding.hiddenLayoutOtherDetails.visibility = View.GONE
            }
        }

        binding.radioButtonExchange.setOnClickListener {
            binding.layoutPreferences.visibility = View.VISIBLE
        }

        binding.radioButtonDonate.setOnClickListener {
            binding.layoutPreferences.visibility = View.GONE
        }

        binding.itemPhotosButton.setOnClickListener {
            choosePhoto()
        }

        binding.deletePhotos.setOnClickListener {
            (it as ImageButton).visibility = View.GONE
            addItemViewModel.photosUri = arrayListOf()
            updatePhotosRecyclerView(addItemViewModel.photosUri)
        }

        binding.itemLocationButton.setOnClickListener {
            val locationDialogFragment = LocationDialogFragment()
            locationDialogFragment.isCancelable = true
            val lat: Double
            val long: Double
            if (addItemViewModel.itemLocation != null) {
                lat = addItemViewModel.itemLocation!!.latitude
                long = addItemViewModel.itemLocation!!.longitude
            } else {
                lat = 0.0
                long = 0.0
            }

            val bundle = Bundle()
            bundle.putDouble("latitude", lat)
            bundle.putDouble("longitude", long)
            locationDialogFragment.arguments = bundle
            locationDialogFragment.show(childFragmentManager, "LocationDialogFragment")
        }

        binding.iconLocationButton.setOnClickListener {
            createSnackBar(resources.getString(R.string.infoLocation), it).show()
        }

        binding.iconPhotosButton.setOnClickListener {
            createSnackBar(resources.getString(R.string.infoPhotos), it).show()
        }

        binding.saveItem.setOnClickListener {

            (it as CircularProgressButton).startAnimation()

            val fieldsOk: Boolean = verifyMandatoryFields()

            if (fieldsOk) {
                // save into Firestore DB sync, and after display the done sign
                addItemViewModel.itemCity =
                    LocationHelper(requireActivity()).getItemCity(addItemViewModel.itemLocation!!)

                addItemViewModel.addItem(object : OneParamCallback<Item> {
                    override fun onComplete(value: Item?) {
                        it.doneLoadingAnimation(
                            R.color.green_light,
                            ContextCompat.getDrawable(
                                requireActivity(),
                                R.drawable.ic_done
                            )!!.toBitmap()
                        )

                        if (value != null) {
                            val action =
                                AddItemFragmentDirections.actionNavAddItemToNavItem(value)
                            requireView().findNavController().navigate(action)
                        }
                    }

                    override fun onError(e: Exception?) {
                        it.revertAnimation()
                        Toast.makeText(
                            requireActivity(),
                            getString(R.string.something_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            } else {
                it.revertAnimation()
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.invalid_fields),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun createSnackBar(text: String, view: View): Snackbar {
        val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE)
            .setAction("OK") {}.setActionTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
            .setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))

        val margin = 15
        val snackbarView: View = snackbar.view
        snackbarView.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.grey_light))
        snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines =
            15

        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        layoutParams.leftMargin = margin
        layoutParams.rightMargin = margin

        snackbarView.layoutParams = layoutParams

        return snackbar
    }

    private fun choosePhoto() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // ask for permission if it is not given already
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(READ_EXTERNAL_STORAGE),
                1
            ) // 1 is standard

        } else {
            val getIntent = Intent(Intent.ACTION_GET_CONTENT)
            getIntent.type = "image/*"
            val pickIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val chooserIntent =
                Intent.createChooser(getIntent, resources.getString(R.string.select_photo))
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

            startActivityForResult(chooserIntent, PICK_IMAGE_CODE)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent != null) {
            val value = parent.getItemAtPosition(position).toString()

            // make first option grey, as a hint
            if (value == getString(R.string.select)) {
                if (view != null)
                    (view as TextView).setTextColor(ContextCompat.getColor(requireContext(),R.color.grey))
            }

            when (parent.id) {
                R.id.spinner_categories -> addItemViewModel.categoryChosen =
                    if (value.equals(
                            resources.getString(R.string.select),
                            true
                        )
                    ) ItemCategory.UNKNOWN
                    else ItemCategory.stringToItemCategory(value)


                R.id.spinner_condition -> addItemViewModel.conditionChosen =
                    if (value.equals(resources.getString(R.string.select), true)) null
                    else ItemCondition.stringToItemCondition(value)
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private fun verifyMandatoryFields(): Boolean {
        var completedFields = true

        // title
        val title = binding.itemTitleEdittext.text.toString()
        if (title.isEmpty()) {
            binding.itemTitleStatus.text = resources.getString(R.string.required)
            binding.itemTitleStatus.visibility = View.VISIBLE
            completedFields = false
        } else if (title.length < MIN_LENGTH_TITLE) {
            binding.itemTitleStatus.text = resources.getString(R.string.item_title_short)
            binding.itemTitleStatus.visibility = View.VISIBLE
            completedFields = false
        } else {
            binding.itemTitleStatus.visibility = View.GONE
        }

        // description
        val description = binding.itemDescriptionEdittext.text.toString()
        if (description.isEmpty()) {
            binding.itemDescriptionStatus.text = resources.getString(R.string.required)
            binding.itemDescriptionStatus.visibility = View.VISIBLE
            completedFields = false
        } else if (description.length < MIN_LENGTH_DESCRIPTION) {
            binding.itemDescriptionStatus.text =
                resources.getString(R.string.item_description_short)
            binding.itemDescriptionStatus.visibility = View.VISIBLE
            completedFields = false
        } else {
            binding.itemDescriptionStatus.visibility = View.GONE
        }

        // purpose & item object
        val forExchange: Boolean? = if (binding.radioButtonExchange.isChecked) {
            binding.itemPurposeStatus.visibility = View.GONE
            true
        } else if (binding.radioButtonDonate.isChecked) {
            binding.itemPurposeStatus.visibility = View.GONE
            false
        } else {
            binding.itemPurposeStatus.text = resources.getString(R.string.required)
            binding.itemPurposeStatus.visibility = View.VISIBLE
            completedFields = false
            null
        }

        // category
        if (addItemViewModel.categoryChosen == ItemCategory.UNKNOWN) {
            binding.itemCategoryStatus.text = resources.getString(R.string.required)
            binding.itemCategoryStatus.visibility = View.VISIBLE
            completedFields = false
        } else {
            binding.itemCategoryStatus.visibility = View.GONE
        }

        // exchange preferences - optional
        val exchangePreferences = getExchangePreferences()

        // photos
        if (addItemViewModel.photosUri.size < MIN_PHOTOS) {
            binding.itemPhotosStatus.text = resources.getString(R.string.required)
            binding.itemPhotosStatus.visibility = View.VISIBLE
            completedFields = false
        } else {
            binding.itemPhotosStatus.visibility = View.GONE
        }

        // location
        if (addItemViewModel.itemLocation == null) {
            binding.itemLocationStatus.text = resources.getString(R.string.required)
            binding.itemLocationStatus.visibility = View.VISIBLE
            completedFields = false
        } else {
            binding.itemLocationStatus.visibility = View.GONE
        }

        // year - optional
        val year = binding.itemYearEdittext.text.toString().ifEmpty { null }

        return if (!completedFields) false
        else {
            addItemViewModel.saveItemData(
                forExchange!!,
                title,
                description,
                year?.toInt(),
                exchangePreferences,
                (requireActivity() as MainActivity).getCurrentUser()!!.email
            )
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        Log.d(TAG, "onActivityResult from AddItemFragment")
        when (requestCode) {
            PICK_IMAGE_CODE -> {
                if (resultCode == RESULT_OK) {
                    shouldCleanUI = false
                    val imageUri: Uri? = intent?.data
                    imageUri?.let {
                        binding.photosRecyclerView.visibility = View.VISIBLE
                        addItemViewModel.photosUri.add(it)
                        binding.deletePhotos.visibility = View.VISIBLE
                        updatePhotosRecyclerView(addItemViewModel.photosUri)
                    }
                }
            }
        }
    }

    private fun updatePhotosRecyclerView(photos: List<Uri>) {
        binding.photosAdapter =
            PhotosRecyclerViewAdapter(photos, requireContext())
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)
        }
    }


    private fun getExchangePreferences(): ArrayList<ItemCategory> {
        val checkboxes: Map<ItemCategory, CheckBox> = mapOf(
            ItemCategory.APPLIANCES to binding.hiddenCategories.categAppliances,
            ItemCategory.CLOTHESSHOES to binding.hiddenCategories.categClothesshoes,
            ItemCategory.DEVICES to binding.hiddenCategories.categDevices,
            ItemCategory.EDUCATION to binding.hiddenCategories.categEducation,
            ItemCategory.FOODDRINK to binding.hiddenCategories.categFooddrink,
            ItemCategory.FURNITURE to binding.hiddenCategories.categFurniture,
            ItemCategory.GARDEN to binding.hiddenCategories.categGarden,
            ItemCategory.GAMES to binding.hiddenCategories.categGames,
            ItemCategory.MEDICAL to binding.hiddenCategories.categMedical
        )

        val result = arrayListOf<ItemCategory>()

        checkboxes.forEach {
            if (it.value.isChecked) {
                result.add(it.key)
            }
        }
        return result
    }

    private fun cleanUIElements() {
        binding.itemTitleEdittext.setText("")
        binding.itemDescriptionEdittext.setText("")

        binding.radioButtonDonate.isChecked = false
        binding.radioButtonDonate.isPressed = false
//        binding.radioButtonDonate.setBackgroundResource(R.drawable.btn_radio_unchecked)

        binding.radioButtonExchange.isChecked = false
        binding.radioButtonExchange.isPressed = false
//        binding.radioButtonExchange.setBackgroundResource(R.drawable.btn_radio_unchecked)

        binding.spinnerCategories.setSelection(0)
        binding.itemYearEdittext.setText("")
        binding.spinnerCondition.setSelection(0)
    }

    override fun saveLocation(location: Place?) {

        addItemViewModel.itemLocation =
            location?.latLng?.let { GeoPoint(it.latitude, it.longitude) }
        if (location != null) {
            binding.locationChosen.apply {
                visibility = View.VISIBLE
                text = location.address
                addItemViewModel.itemAddress = location.address
            }
            binding.itemLocationButton.text = getString(R.string.change_location)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "AddItemFragment is onDestroyView")
        addItemViewModel.restoreDefaultValues()
//        cleanUIElements()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "AddItemFragment is onStop")
        shouldCleanUI = false
    }
}