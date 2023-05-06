package com.diana.bachelorthesis.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ItemsRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentFavoriteDonationsBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.viewmodel.FavoritesViewModel

class FavoriteDonationsFragment : Fragment() {
    private val TAG: String = FavoriteDonationsFragment::class.java.name
    private var _binding: FragmentFavoriteDonationsBinding? = null

    private val binding get() = _binding!!
    private lateinit var favoritesViewModel: FavoritesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "FavoriteDonationsFragment is onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "FavoriteDonationsFragment is onCreateView")
        _binding = FragmentFavoriteDonationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "FavoriteDonationsFragment is onViewCreated")
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.itemsAdapter = ItemsRecyclerViewAdapter(listOf(), requireContext()) {}
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "FavoriteDonationsFragment is onActivityCreated")
        favoritesViewModel = ViewModelProvider(requireParentFragment())[FavoritesViewModel::class.java]
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "FavoriteDonationsFragment is onStart")
        updateRecyclerView(listOf(), true)
        favoritesViewModel.favoriteDonationsIds = (requireActivity() as MainActivity).getCurrentUser()!!.favoriteDonations
        favoritesViewModel.populateLiveDataDonations()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "FavoriteDonationsFragment is onResume")
        favoritesViewModel.donationItems.observe(viewLifecycleOwner) {
            updateRecyclerView(it)
            (parentFragment as FavoritesFragment).scrollRecyclerView(binding.recyclerView)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "FavoriteDonationsFragment is onPause")
    }

     private fun updateRecyclerView(items: List<Item>, progressBarAppears: Boolean = false) {
         if (progressBarAppears) {
             binding.progressBar.visibility = View.VISIBLE
             binding.recyclerView.visibility = View.INVISIBLE
             binding.textNoItems.visibility = View.INVISIBLE
             binding.textNumberItems.text = resources.getString(R.string.loading)
         } else if (items.isNotEmpty()) {
             binding.recyclerView.visibility = View.VISIBLE
             binding.progressBar.visibility = View.INVISIBLE
             binding.textNoItems.visibility = View.INVISIBLE
             binding.itemsAdapter =
                 ItemsRecyclerViewAdapter(items, requireContext()) { item ->
                     val action = FavoritesFragmentDirections.actionNavFavoritesToNavItem(item)
                     requireView().findNavController().navigate(action)
                 }
             binding.textNumberItems.text = items.size.toString()
         } else {
             binding.progressBar.visibility = View.INVISIBLE
             binding.recyclerView.visibility = View.INVISIBLE
             binding.textNoItems.visibility = View.VISIBLE
             binding.textNumberItems.text = items.size.toString()
             binding.textNoItems.text = "Nothing... \n Tap ♡ in an item's page and you will have it saved here."
         }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "FavoriteDonationsFragment is onDestroyView")
    }
}