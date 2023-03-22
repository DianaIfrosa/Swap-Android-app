package com.diana.bachelorthesis.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.FragmentAddItemBinding
import com.diana.bachelorthesis.databinding.FragmentHomeBinding
import com.diana.bachelorthesis.viewmodel.AddItemViewModel
import com.diana.bachelorthesis.viewmodel.HomeViewModel

class AddItemFragment : Fragment() {

    private var _binding: FragmentAddItemBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val addItemViewModel =
            ViewModelProvider(this).get(AddItemViewModel::class.java)

        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textAddItem
//        addItemViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}