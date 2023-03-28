package com.diana.bachelorthesis.view

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources.NotFoundException
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apachat.loadingbutton.core.customViews.CircularProgressButton
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.PhotosRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentAddItemBinding
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.model.ItemCondition
import com.diana.bachelorthesis.viewmodel.AddItemViewModel


class AddItemFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private val TAG: String = AddItemFragment::class.java.name
    private val MIN_LENGTH_TITLE = 3
    private val MIN_LENGTH_DESCRIPTION = 5

    lateinit var addItemViewModel: AddItemViewModel
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var _binding: FragmentAddItemBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        addItemViewModel = ViewModelProvider(this).get(AddItemViewModel::class.java)

        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val recyclerView: RecyclerView = binding.photosRecyclerView
        val horizontalLayoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = horizontalLayoutManager
        updatePhotosRecyclerView(arrayListOf())

        initButtonListeners()
        attachCategoryAdapter()
        attachConditionAdapter()

        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerActivityForResult()
    }

    // TODO make a general function for spinner adapter that returns the adapter and receives the arrayList of elements
    private fun attachCategoryAdapter() {
        val spinnerCategories = binding.spinnerCategories
        spinnerCategories.onItemSelectedListener = this
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
            requireContext(),
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
                    view.setTextColor(resources.getColor(R.color.grey))
                return view
            }
        }
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerCategories.adapter = categoriesAdapter
    }

    private fun attachConditionAdapter() {
        val spinnerCondition = binding.spinnerCondition
        spinnerCondition.onItemSelectedListener = this
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
            requireContext(),
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
                    view.setTextColor(resources.getColor(R.color.grey))
                return view
            }
        }
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerCondition.adapter = conditionAdapter
    }

    private fun initButtonListeners() {
        binding.textOtherDetails.setOnClickListener {
            if (binding.hiddenLayoutOtherDetails.visibility == View.GONE) {
                (it as TextView).setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_arrow_dropup,
                    0
                )
                binding.hiddenLayoutOtherDetails.visibility = View.VISIBLE
            } else {
                (it as TextView).setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_arrow_dropdown,
                    0
                )
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
            addItemViewModel.itemPhotos = arrayListOf()
            updatePhotosRecyclerView(addItemViewModel.itemPhotos)
        }

        binding.saveItem.setOnClickListener {

            (it as CircularProgressButton).startAnimation()

            val fieldsOk: Boolean = verifyMandatoryFields()

            if (fieldsOk) {
                // save into Firestore DB async, and after display the done sign
                it.doneLoadingAnimation(
                    Color.GREEN,
                    BitmapFactory.decodeResource(resources, R.drawable.ic_done)
                )
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

    private fun choosePhoto() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // cere permisiune daca nu e data deja
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(READ_EXTERNAL_STORAGE),
                1
            ) // 1 e standard

        } else {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent != null) {
            val value = parent.getItemAtPosition(position).toString()

            // make first option grey, as a hint
            if (value == getString(R.string.select)) {
                if (view != null)
                    (view as TextView).setTextColor(resources.getColor(R.color.grey))
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
            binding.itemTitleStatus.visibility = View.INVISIBLE
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
            binding.itemDescriptionStatus.visibility = View.INVISIBLE
        }

        // purpose & item object
        val forExchange: Boolean? = if (binding.radioButtonExchange.isChecked) {
            binding.itemPurposeStatus.visibility = View.INVISIBLE
            true
        } else if (binding.radioButtonDonate.isChecked) {
            binding.itemPurposeStatus.visibility = View.INVISIBLE
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
            binding.itemCategoryStatus.visibility = View.INVISIBLE
        }

        // exchange preferences
        val exchangePreferences = getExchangePreferences()

        // photos
        if (addItemViewModel.itemPhotos.isEmpty()) {
            binding.itemPhotosStatus.text = resources.getString(R.string.required)
            binding.itemPhotosStatus.visibility = View.VISIBLE
            completedFields = false
        } else {
            binding.itemPhotosStatus.visibility = View.INVISIBLE
        }

        // location
        if (addItemViewModel.itemLocation == null) {
            binding.itemLocationStatus.text = resources.getString(R.string.required)
            binding.itemLocationStatus.visibility = View.VISIBLE
            completedFields = false
        } else {
            binding.itemLocationStatus.visibility = View.INVISIBLE
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
                exchangePreferences
            )
            true
        }
    }

    private fun registerActivityForResult(): Uri? {
        var imageUri: Uri? = null
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val resultCode = result.resultCode
                val imageData = result.data

                if (resultCode == RESULT_OK && imageData != null) {
                    imageUri = imageData.data

                    imageUri?.let {
                        binding.photosRecyclerView.visibility = View.VISIBLE
                        addItemViewModel.itemPhotos.add(it)
                        binding.deletePhotos.visibility = View.VISIBLE
                        updatePhotosRecyclerView(addItemViewModel.itemPhotos)
                    }
                }
            }

        return imageUri
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
            ItemCategory.APPLIANCES to binding.categories.categAppliances,
            ItemCategory.CLOTHESSHOES to binding.categories.categClothesshoes,
            ItemCategory.DEVICES to binding.categories.categDevices,
            ItemCategory.EDUCATION to binding.categories.categEducation,
            ItemCategory.FOODDRINK to binding.categories.categFooddrink,
            ItemCategory.FURNITURE to binding.categories.categFurniture,
            ItemCategory.GARDEN to binding.categories.categGarden,
            ItemCategory.MEDICAL to binding.categories.categMedical
        )

        val result = arrayListOf<ItemCategory>()

        checkboxes.forEach {
            if (it.value.isChecked) {
                result.add(it.key)
            }
        }
        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}