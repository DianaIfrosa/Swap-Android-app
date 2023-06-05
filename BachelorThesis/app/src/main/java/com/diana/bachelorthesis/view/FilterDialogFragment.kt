package com.diana.bachelorthesis.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.CheckBox
import androidx.fragment.app.DialogFragment
import com.diana.bachelorthesis.databinding.FragmentFilterDialogBinding
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.utils.SortFilterDialogListener
import java.util.ArrayList

class FilterDialogFragment : DialogFragment() {
    private val TAG: String = FilterDialogFragment::class.java.name

    private var _binding: FragmentFilterDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var fragmentParent: HomeFragment
    private var cities: ArrayList<String> = arrayListOf()
    private var chosenCity = ""
    private var chosenCategories: MutableList<ItemCategory> = arrayListOf()
    private lateinit var checkboxes: Map<ItemCategory, CheckBox>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "FilterDialogFragment is onCreate")
        restoreValues()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "FilterDialogFragment is onCreateView")
        _binding = FragmentFilterDialogBinding.inflate(inflater, container, false)
        val root: View = binding.root
        fragmentParent = parentFragment as HomeFragment
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "FilterDialogFragment is onViewCreated")

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cities)
        binding.filterCityAutoComplete.setAdapter(adapter)

        checkboxes = mapOf(
            ItemCategory.ACCESSORIES to binding.categories.categAccessories,
            ItemCategory.APPLIANCES to binding.categories.categAppliances,
            ItemCategory.BEAUTY to binding.categories.categBeauty,
            ItemCategory.CLOTHESSHOES to binding.categories.categClothesshoes,
            ItemCategory.DEVICES to binding.categories.categDevices,
            ItemCategory.EDUCATION to binding.categories.categEducation,
            ItemCategory.FOODDRINK to  binding.categories.categFooddrink ,
            ItemCategory.FURNITURE to  binding.categories.categFurniture,
            ItemCategory.GAMES to binding.categories.categGames,
            ItemCategory.GARDEN to binding.categories.categGarden,
            ItemCategory.HOMEDECOR to binding.categories.categHomeDecor,
            ItemCategory.JEWELRY to binding.categories.categJewelry,
            ItemCategory.MEDICAL to binding.categories.categMedical
        )
        applySelections()

        binding.filterCityAutoComplete.setOnItemClickListener { _, _, position, _ ->
            chosenCity = adapter.getItem(position) ?: ""
        }

        binding.filterApply.setOnClickListener {
            val listener: SortFilterDialogListener = fragmentParent
            chosenCity = binding.filterCityAutoComplete.text.toString()
            chosenCategories = getSelectedCheckboxes()
            listener.saveFilterOptions(requireActivity(), chosenCity, chosenCategories)
            dialog!!.dismiss()
        }

        binding.filterCancel.setOnClickListener {
            dialog!!.dismiss()
        }
    }

    private fun restoreValues() {
        val bundle = this.arguments
        cities = bundle?.getStringArrayList("cities") ?: arrayListOf()

        chosenCity = bundle?.getString("chosenCity") ?: ""
        val categories = bundle?.getStringArrayList("chosenCategories") ?: arrayListOf()
        categories.forEach {
            chosenCategories.add(ItemCategory.valueOf(it))
        }
    }

    private fun applySelections() {
        if (chosenCity.isNotEmpty()) {
            binding.filterCityAutoComplete.setText(chosenCity)
        }

        if (chosenCategories.isNotEmpty()) {
            chosenCategories.forEach { category->
                checkboxes[category]?.isChecked = true
            }
        }
    }

    private fun getSelectedCheckboxes(): MutableList<ItemCategory> {
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
        Log.d(TAG, "FilterDialogFragment is onDestroyView")
        _binding = null
    }

}