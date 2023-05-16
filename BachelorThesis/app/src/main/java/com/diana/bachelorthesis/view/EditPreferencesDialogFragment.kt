package com.diana.bachelorthesis.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.CheckBox
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ListAdapterPreferences
import com.diana.bachelorthesis.databinding.FragmentEditPreferencesDialogBinding
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.utils.HelperListAdapter
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.ProfileOptionsListener
import com.diana.bachelorthesis.viewmodel.ProfileViewModel


class EditPreferencesDialogFragment : DialogFragment() {
    private val TAG: String = EditPreferencesDialogFragment::class.java.name

    private var _binding: FragmentEditPreferencesDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var fragmentParent: ProfileFragment
    private lateinit var toolbar: Toolbar
    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var adapterOwners: ListAdapterPreferences
    private lateinit var adapterCities: ListAdapterPreferences
    private lateinit var adapterWords: ListAdapterPreferences

    var checkboxesCategories: Map<ItemCategory, CheckBox> = mapOf()
    var checkboxesExchangePreferences: Map<ItemCategory, CheckBox> = mapOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "EditPreferencesDialogFragment is onCreate")
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
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
        profileViewModel = ViewModelProvider(fragmentParent)[ProfileViewModel::class.java]
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        initCheckboxes()
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

        fragmentParent.getAllCities(object : ListParamCallback<String> {
            override fun onComplete(values: ArrayList<String>) {
                adapterCitiesAutocomplete =
                    ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        values
                    )
                binding.autocompleteCities.setAdapter(adapterCitiesAutocomplete)
            }

            override fun onError(e: Exception?) {}
        })

        fragmentParent.getAllOwners(object : ListParamCallback<String> {
            override fun onComplete(values: ArrayList<String>) {
                adapterOwnersAutocomplete =
                    ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        values
                    )
                binding.autocompleteOwners.setAdapter(adapterOwnersAutocomplete)
            }

            override fun onError(e: Exception?) {}
        })
    }

    private fun initListeners() {
        binding.btnAddCity.setOnClickListener {
            val city = binding.autocompleteCities.text.toString()
            if (city.isNotEmpty()) {
                profileViewModel.preferredCities = adapterCities.getItems()
                profileViewModel.preferredCities.add(city)
                adapterCities = ListAdapterPreferences(
                    requireContext(),
                    ArrayList(profileViewModel.preferredCities)
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
                profileViewModel.preferredOwners = adapterOwners.getItems()
                profileViewModel.preferredOwners.add(owner)
                adapterOwners = ListAdapterPreferences(
                    requireContext(),
                    ArrayList(profileViewModel.preferredOwners)
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
                profileViewModel.preferredWords = adapterWords.getItems()
                profileViewModel.preferredWords.add(word)
                adapterWords = ListAdapterPreferences(
                    requireContext(),
                    ArrayList(profileViewModel.preferredWords)
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
        if (view != null) {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun initCheckboxes() {
        checkboxesCategories = mapOf(
            ItemCategory.APPLIANCES to binding.categories.categAppliances,
            ItemCategory.CLOTHESSHOES to binding.categories.categClothesshoes,
            ItemCategory.DEVICES to binding.categories.categDevices,
            ItemCategory.EDUCATION to binding.categories.categEducation,
            ItemCategory.FOODDRINK to binding.categories.categFooddrink,
            ItemCategory.FURNITURE to binding.categories.categFurniture,
            ItemCategory.GAMES to binding.categories.categGames,
            ItemCategory.GARDEN to binding.categories.categGarden,
            ItemCategory.MEDICAL to binding.categories.categMedical
        )

        checkboxesExchangePreferences = mapOf(
            ItemCategory.APPLIANCES to binding.exchangePreferences.categAppliances,
            ItemCategory.CLOTHESSHOES to binding.exchangePreferences.categClothesshoes,
            ItemCategory.DEVICES to binding.exchangePreferences.categDevices,
            ItemCategory.EDUCATION to binding.exchangePreferences.categEducation,
            ItemCategory.FOODDRINK to binding.exchangePreferences.categFooddrink,
            ItemCategory.FURNITURE to binding.exchangePreferences.categFurniture,
            ItemCategory.GAMES to binding.exchangePreferences.categGames,
            ItemCategory.GARDEN to binding.exchangePreferences.categGarden,
            ItemCategory.MEDICAL to binding.exchangePreferences.categMedical
        )
    }


    private fun updateUI() {
        // categories
        if (profileViewModel.preferredCategories.isNotEmpty()) {
            profileViewModel.preferredCategories.forEach { category ->
                checkboxesCategories[category]?.isChecked = true
            }
        }

        // exchange preferences
        if (profileViewModel.preferredExchangePreferences.isNotEmpty()) {
            profileViewModel.preferredExchangePreferences.forEach { category ->
                checkboxesExchangePreferences[category]?.isChecked = true
            }
        }

        // owners
        adapterOwners = ListAdapterPreferences(
            requireContext(),
            ArrayList(profileViewModel.preferredOwners)
        )
        binding.ownersList.adapter = adapterOwners
        HelperListAdapter.adjustListViewSize(binding.ownersList)

        // cities
        adapterCities = ListAdapterPreferences(
            requireContext(),
            ArrayList(profileViewModel.preferredCities)
        )
        binding.citiesList.adapter = adapterCities
        HelperListAdapter.adjustListViewSize(binding.citiesList)

        // words
        adapterWords = ListAdapterPreferences(
            requireContext(),
            ArrayList(profileViewModel.preferredWords)
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
        profileViewModel.preferredCategories = selectedCategories

        val selectedExchangePreferences = arrayListOf<ItemCategory>()
        checkboxesExchangePreferences.forEach {
            if (it.value.isChecked) {
                selectedExchangePreferences.add(it.key)
            }
        }
        profileViewModel.preferredExchangePreferences = selectedExchangePreferences
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

            profileViewModel.preferredWords = adapterWords.getItems()
            profileViewModel.preferredOwners = adapterOwners.getItems()
            profileViewModel.preferredCities = adapterCities.getItems()

            listener.savePreferencesForRecommendations(
                profileViewModel.preferredWords,
                profileViewModel.preferredOwners,
                profileViewModel.preferredCities,
                profileViewModel.preferredCategories,
                profileViewModel.preferredExchangePreferences
            )
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