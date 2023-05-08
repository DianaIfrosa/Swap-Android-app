package com.diana.bachelorthesis.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.DialogFragment
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ListAdapter
import com.diana.bachelorthesis.databinding.FragmentEditPreferencesDialogBinding
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.utils.HelperListAdapter
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.ProfileOptionsListener


class EditPreferencesDialogFragment : DialogFragment() {
    private val TAG: String = EditPreferencesDialogFragment::class.java.name

    private var _binding: FragmentEditPreferencesDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var fragmentParent: ProfileFragment
    private lateinit var toolbar: Toolbar

    private var preferredOwners: MutableList<String> = mutableListOf()
    private var preferredCities: MutableList<String> = mutableListOf()
    private var preferredWords: MutableList<String> = mutableListOf()
    private var preferredCategories: MutableList<ItemCategory> = mutableListOf()
    private var preferredExchangePreferences: MutableList<ItemCategory> = mutableListOf()

    private lateinit var checkboxesCategories: Map<ItemCategory, CheckBox>
    private lateinit var checkboxesExchangePreferences: Map<ItemCategory, CheckBox>

    private lateinit var adapterOwners: ListAdapter
    private lateinit var adapterCities: ListAdapter
    private lateinit var adapterWords: ListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "EditPreferencesDialogFragment is onCreate")
        setStyle(STYLE_NORMAL,  R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "EditPreferencesDialogFragment is onCreateView")
        _binding = FragmentEditPreferencesDialogBinding.inflate(inflater, container, false)
        val root: View = binding.root

        toolbar = root.findViewById(R.id.toolbar_dialog_preferences)
        customizeToolbar()

        fragmentParent = parentFragment as ProfileFragment
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        initCheckboxes()
        changeSectionsColor()
        restoreValues()
        updateUI()
        setAdaptersAutocomplete()
        initListeners()

        return root
    }

    private fun setAdaptersAutocomplete() {
        val cities = listOf<String>()
        var adapterCitiesAutocomplete =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cities)
        binding.autocompleteCities.setAdapter(adapterCitiesAutocomplete)

        val owners = listOf<String>()
        var adapterOwnersAutocomplete =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, owners)
        binding.autocompleteCities.setAdapter(adapterOwnersAutocomplete)

        fragmentParent.getAllCities(object: ListParamCallback<String> {
            override fun onComplete(values: ArrayList<String>) {
                adapterCitiesAutocomplete =
                    ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, values)
                binding.autocompleteCities.setAdapter(adapterCitiesAutocomplete)
            }

            override fun onError(e: Exception?) {}
        })

        fragmentParent.getAllOwners(object: ListParamCallback<String> {
            override fun onComplete(values: ArrayList<String>) {
                adapterOwnersAutocomplete =
                    ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, values)
                binding.autocompleteOwners.setAdapter(adapterOwnersAutocomplete)
            }

            override fun onError(e: Exception?) {}
        })
    }

    private fun initListeners() {
        binding.btnAddCity.setOnClickListener {
            val city = binding.autocompleteCities.text.toString()
            if (city.isNotEmpty()) {
                preferredCities = adapterCities.getItems()
                preferredCities.add(city)
                adapterCities = ListAdapter(
                    requireContext(),
                    ArrayList(preferredCities)
                )
                binding.citiesList.adapter = adapterCities
                HelperListAdapter.adjustListViewSize(binding.citiesList)
                adapterCities.notifyDataSetChanged()
            }
            binding.autocompleteCities.text.clear()
            closeKeyboard()
            binding.autocompleteCities.clearFocus()
        }

        binding.btnAddOwner.setOnClickListener {
            val owner = binding.autocompleteOwners.text.toString()
            if (owner.isNotEmpty()) {
                preferredOwners = adapterOwners.getItems()
                preferredOwners.add(owner)
                adapterOwners = ListAdapter(
                    requireContext(),
                    ArrayList(preferredOwners)
                )
                binding.ownersList.adapter = adapterOwners
                HelperListAdapter.adjustListViewSize(binding.ownersList)
                adapterOwners.notifyDataSetChanged()
            }
            binding.autocompleteOwners.text.clear()
            closeKeyboard()
            binding.autocompleteOwners.clearFocus()
        }

        binding.btnAddWord.setOnClickListener {
            val word = binding.editextWords.text.toString()
            if (word.isNotEmpty()) {
                preferredWords = adapterWords.getItems()
                preferredWords.add(word)
                adapterWords = ListAdapter(
                    requireContext(),
                    ArrayList(preferredWords)
                )
                binding.wordsList.adapter = adapterWords
                HelperListAdapter.adjustListViewSize(binding.wordsList)
                adapterWords.notifyDataSetChanged()
            }
            binding.editextWords.text.clear()
            closeKeyboard()
            binding.editextWords.clearFocus()
        }
    }

    private fun closeKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null)
        {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun initCheckboxes() {
        checkboxesCategories = mapOf(
            ItemCategory.APPLIANCES to binding.categories.categAppliances,
            ItemCategory.CLOTHESSHOES to binding.categories.categClothesshoes,
            ItemCategory.DEVICES to binding.categories.categDevices,
            ItemCategory.EDUCATION to binding.categories.categEducation,
            ItemCategory.FOODDRINK to  binding.categories.categFooddrink ,
            ItemCategory.FURNITURE to  binding.categories.categFurniture,
            ItemCategory.GAMES to binding.categories.categGames,
            ItemCategory.GARDEN to binding.categories.categGarden,
            ItemCategory.MEDICAL to binding.categories.categMedical
        )

        checkboxesExchangePreferences = mapOf(
            ItemCategory.APPLIANCES to binding.exchangePreferences.categAppliances,
            ItemCategory.CLOTHESSHOES to binding.exchangePreferences.categClothesshoes,
            ItemCategory.DEVICES to binding.exchangePreferences.categDevices,
            ItemCategory.EDUCATION to binding.exchangePreferences.categEducation,
            ItemCategory.FOODDRINK to  binding.exchangePreferences.categFooddrink ,
            ItemCategory.FURNITURE to  binding.exchangePreferences.categFurniture,
            ItemCategory.GAMES to binding.exchangePreferences.categGames,
            ItemCategory.GARDEN to binding.exchangePreferences.categGarden,
            ItemCategory.MEDICAL to binding.exchangePreferences.categMedical
        )
    }

    private fun changeSectionsColor() {
        changeSectionColor(binding.wordsSection, resources.getColor(R.color.blue_pale))
        changeSectionColor(binding.ownersSection, resources.getColor(R.color.turquoise_pale))
        changeSectionColor(binding.categoriesSection, resources.getColor(R.color.green_pale))
        changeSectionColor(binding.citiesSection, resources.getColor(R.color.salmon_pale))
        changeSectionColor(binding.exchangePreferencesSection, resources.getColor(R.color.salmon_red_pale))
    }

    private fun changeSectionColor(section: LinearLayout, color: Int) {
        val unwrappedDrawable: Drawable =
            AppCompatResources.getDrawable(requireContext(), R.drawable.tab_indicator)!!
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable)
        DrawableCompat.setTint(wrappedDrawable, color)

        section.background = wrappedDrawable
    }

    private fun restoreValues() {
        val bundle = this.arguments

        val categories = bundle?.getStringArrayList("categories") ?: mutableListOf()
        val chosenCategories = arrayListOf<ItemCategory>()
        categories.forEach {
            chosenCategories.add(ItemCategory.valueOf(it))
        }
        preferredCategories = chosenCategories
        preferredCities = bundle?.getStringArrayList("cities") ?: mutableListOf()
        preferredOwners = bundle?.getStringArrayList("owners") ?: mutableListOf()
        preferredWords = bundle?.getStringArrayList("words") ?: mutableListOf()

        val exchangePreferences = bundle?.getStringArrayList("exchange_preferences") ?: mutableListOf()
        val chosenPreferences = arrayListOf<ItemCategory>()
        exchangePreferences.forEach {
            chosenPreferences.add(ItemCategory.valueOf(it))
        }
        preferredExchangePreferences = chosenPreferences
    }

    private fun updateUI() {
        // categories
        if (preferredCategories.isNotEmpty()) {
            preferredCategories.forEach { category->
                checkboxesCategories[category]?.isChecked = true
            }
        }

        // exchange preferences
        if (preferredExchangePreferences.isNotEmpty()) {
            preferredExchangePreferences.forEach { category->
                checkboxesExchangePreferences[category]?.isChecked = true
            }
        }

        // owners
        adapterOwners = ListAdapter(
            requireContext(),
            ArrayList(preferredOwners)
        )
        binding.ownersList.adapter = adapterOwners
        HelperListAdapter.adjustListViewSize(binding.ownersList)

        // cities
        adapterCities = ListAdapter(
            requireContext(),
            ArrayList(preferredCities)
        )
        binding.citiesList.adapter = adapterCities
        HelperListAdapter.adjustListViewSize(binding.citiesList)

        // words
        adapterWords = ListAdapter(
            requireContext(),
            ArrayList(preferredWords)
        )
        binding.wordsList.adapter = adapterWords
        HelperListAdapter.adjustListViewSize(binding.wordsList)
    }

    private fun getCheckboxesSelections() {
        val selectedCategories = arrayListOf<ItemCategory>()
        checkboxesCategories.forEach {
            if (it.value.isChecked) {
                selectedCategories.add(it.key)
            }
        }
        preferredCategories = selectedCategories

        val selectedExchangePreferences = arrayListOf<ItemCategory>()
        checkboxesExchangePreferences.forEach {
            if (it.value.isChecked) {
                selectedExchangePreferences.add(it.key)
            }
        }
        preferredExchangePreferences = selectedExchangePreferences
    }

    private fun customizeToolbar() {
        toolbar.title = getString(R.string.edit_preferences)
        toolbar.setTitleTextColor(resources.getColor(R.color.purple_dark))
        toolbar.setNavigationOnClickListener {
            dialog!!.dismiss()
        }
        toolbar.inflateMenu(R.menu.dialog_fragment_menu)
        toolbar.setOnMenuItemClickListener {
            val listener: ProfileOptionsListener = fragmentParent
            getCheckboxesSelections()

            preferredWords = adapterWords.getItems()
            preferredOwners = adapterOwners.getItems()
            preferredCities = adapterCities.getItems()

            listener.savePreferencesForRecommendations(preferredWords, preferredOwners, preferredCities, preferredCategories, preferredExchangePreferences)
            dialog!!.dismiss()
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "EditPreferencesDialogFragment is onDestroyView")
    }
}