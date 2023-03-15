package com.diana.bachelorthesis.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.diana.bachelorthesis.databinding.FragmentSortDialogBinding
import com.diana.bachelorthesis.view.HomeFragment

class SortDialogFragment : DialogFragment() {
    private val TAG: String = SortDialogFragment::class.java.name

    lateinit var radioButtonsGroup: RadioGroup
    private var _binding: FragmentSortDialogBinding? = null
    private val binding get() = _binding!!
    lateinit var fragmentParent: HomeFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSortDialogBinding.inflate(inflater, container, false)
        val root: View = binding.root
        fragmentParent = parentFragment as HomeFragment
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radioButtonsGroup = binding.sortRadioButtons
        restoreSortOption()

        binding.sortApply.setOnClickListener {

            val selectedRadioButtonId = radioButtonsGroup.checkedRadioButtonId
            val index = radioButtonsGroup.indexOfChild(radioButtonsGroup.findViewById(selectedRadioButtonId))

            fragmentParent.saveSortOption(index)
            fragmentParent.homeViewModel.sort(index)
            fragmentParent.updateRecyclerView(fragmentParent.homeViewModel.currentItems)

            dialog!!.dismiss()
        }

        binding.sortCancel.setOnClickListener {
            dialog!!.dismiss()
        }

    }

    private fun restoreSortOption() {
        val option = (parentFragment as HomeFragment).getSavedSortOption()
        (radioButtonsGroup.getChildAt(option) as RadioButton).isChecked = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "SortDialogFragment is destroyed")
    }
}