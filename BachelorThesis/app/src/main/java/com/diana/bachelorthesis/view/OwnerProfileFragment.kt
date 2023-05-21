package com.diana.bachelorthesis.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ItemsHorizontalRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentOwnerProfileBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.viewmodel.OwnerProfileViewModel
import com.diana.bachelorthesis.viewmodel.UserViewModel
import com.squareup.picasso.Picasso
import java.lang.Exception

class OwnerProfileFragment : Fragment(), BasicFragment {
    private val TAG: String = OwnerProfileFragment::class.java.name
    private lateinit var userViewModel: UserViewModel
    private var _binding: FragmentOwnerProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var ownerProfileViewModel: OwnerProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "OwnerProfileFragment is onCreateView")
        _binding = FragmentOwnerProfileBinding.inflate(layoutInflater)
        val root: View = binding.root

        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        updateRecyclerView(arrayListOf(), true)
        return root
    }

    private fun getViewModels() {
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        ownerProfileViewModel = ViewModelProvider(this)[OwnerProfileViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "OwnerProfileFragment is onViewCreated")
        getViewModels()
        ownerProfileViewModel.owner = OwnerProfileFragmentArgs.fromBundle(requireArguments()).owner
        setSubPageAppbar(requireActivity(), ownerProfileViewModel.owner.name)

        updateUIElements()
        ownerProfileViewModel.getItemsFromOwner()
        ownerProfileViewModel.allItems.observe(viewLifecycleOwner) {
            if (it != null) {
                updateRecyclerView(it)
            } else {
                Toast.makeText(requireActivity(), getString(R.string.something_failed), Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun updateUIElements() {
        binding.profileName.text = ownerProfileViewModel.owner.name
        Picasso.get().load(ownerProfileViewModel.owner.profilePhoto).into(binding.ownerPhoto)
    }

    fun updateRecyclerView(items: List<Item>, progressBarAppears: Boolean = false) {

        if (binding != null) {
            if (progressBarAppears) {
                binding.progressBar.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                binding.noOtherPostsText.visibility = View.GONE
            } else if (items.isNotEmpty()) {
                binding.recyclerView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.noOtherPostsText.visibility = View.GONE
                val adapter =
                    ItemsHorizontalRecyclerViewAdapter(
                        items,
                        false,
                        requireContext(),
                        null
                    ) { item ->
                        val action =
                            OwnerProfileFragmentDirections.actionNavOwnerProfileToNavItem(item)
                        requireView().findNavController().navigate(action)
                    }
                binding.recyclerView.adapter = adapter
            } else {
                binding.recyclerView.visibility = View.GONE
                binding.progressBar.visibility = View.GONE

                binding.noOtherPostsText.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "OwnerProfileFragment is onDestroyView")
        _binding = null
    }

    override fun initListeners() {

    }
}