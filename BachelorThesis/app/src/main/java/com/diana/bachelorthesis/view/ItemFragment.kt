package com.diana.bachelorthesis.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentItemBinding
import com.diana.bachelorthesis.databinding.FragmentLocationDialogBinding
import com.diana.bachelorthesis.utils.BasicFragment

class ItemFragment : Fragment(), BasicFragment {

    private val TAG: String = ItemFragment::class.java.name
    private var _binding: FragmentItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "ItemFragment is on onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "ItemFragment is on onCreate")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "ItemFragment is on onViewCreated")
    }

    override fun initListeners() {
        TODO("Not yet implemented")
    }

    override fun setAppbar() {
        TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "ItemFragment is on onDestroyView")
        _binding = null
    }

}