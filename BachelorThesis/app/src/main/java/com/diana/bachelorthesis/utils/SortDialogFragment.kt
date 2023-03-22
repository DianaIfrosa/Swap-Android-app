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

    private lateinit var radioButtonsGroup: RadioGroup
    private var _binding: FragmentSortDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var fragmentParent: HomeFragment

    private var sortOption = 0

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = this.arguments
        sortOption = bundle?.getInt("sortOption") ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radioButtonsGroup = binding.sortRadioButtons
        restoreSortOption()

        binding.sortApply.setOnClickListener {

            val selectedRadioButtonId = radioButtonsGroup.checkedRadioButtonId
            val index =
                radioButtonsGroup.indexOfChild(radioButtonsGroup.findViewById(selectedRadioButtonId))

            val listener: SortFilterDialogListener = fragmentParent
            listener.saveSortOption(index)

            dialog!!.dismiss()
        }

        binding.sortCancel.setOnClickListener {
            dialog!!.dismiss()
        }

    }

    private fun restoreSortOption() {
        (radioButtonsGroup.getChildAt(sortOption) as RadioButton).isChecked = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "SortDialogFragment is destroyed")
    }
}