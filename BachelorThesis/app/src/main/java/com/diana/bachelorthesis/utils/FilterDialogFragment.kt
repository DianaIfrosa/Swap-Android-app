package com.diana.bachelorthesis.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentFilterDialogBinding
import com.diana.bachelorthesis.databinding.FragmentSortDialogBinding
import com.diana.bachelorthesis.view.HomeFragment

class FilterDialogFragment : DialogFragment() {
    private val TAG: String = FilterDialogFragment::class.java.name

    private var _binding: FragmentFilterDialogBinding? = null
    private val binding get() = _binding!!
    lateinit var fragmentParent: HomeFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFilterDialogBinding.inflate(inflater, container, false)
        val root: View = binding.root
        fragmentParent = parentFragment as HomeFragment
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.filterApply.setOnClickListener {
            dialog!!.dismiss()
        }

        binding.filterCancel.setOnClickListener {
            dialog!!.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "FilterDialogFragment is destroyed")
    }

    // TODO filtrare dupa categorie, dupa owner (cautare dupa nume), oras, tara?
}