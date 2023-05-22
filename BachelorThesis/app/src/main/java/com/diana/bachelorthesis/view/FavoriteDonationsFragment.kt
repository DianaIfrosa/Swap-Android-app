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
import com.diana.bachelorthesis.viewmodel.FavoriteDonationsViewModel

class FavoriteDonationsFragment : Fragment() {
    private val TAG: String = FavoriteDonationsFragment::class.java.name
    private var _binding: FragmentFavoriteDonationsBinding? = null

    private val binding get() = _binding!!
    private lateinit var favoritesViewModel: FavoriteDonationsViewModel

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
        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        binding.itemsAdapter = ItemsRecyclerViewAdapter(listOf(), requireActivity()) {}
        updateRecyclerView(listOf(), true)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "FavoriteDonationsFragment is onViewCreated")
        favoritesViewModel = ViewModelProvider(this)[FavoriteDonationsViewModel::class.java]
        favoritesViewModel.favoriteDonationsIds = (requireActivity() as MainActivity).getCurrentUser()!!.favoriteDonations
        favoritesViewModel.populateLiveDataDonations()
        favoritesViewModel.donationItems.observe(viewLifecycleOwner) {items ->
//            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            Log.d(TAG, "Observer called")
                updateRecyclerView(items)
//            (parentFragment as FavoritesFragment).scrollRecyclerView(binding.recyclerView)
//            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "FavoriteDonationsFragment is onStart")

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "FavoriteDonationsFragment is onResume")

    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "FavoriteDonationsFragment is onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "FavoriteDonationsFragment is onStop")
//        favoritesViewModel.lastScrollPosition =
//            (binding.recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
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
                 ItemsRecyclerViewAdapter(items, requireActivity()) { item ->
                     val action = FavoritesFragmentDirections.actionNavFavoritesToNavItem(item)
                     requireView().findNavController().navigate(action)
                 }
             binding.textNumberItems.text = items.size.toString()
         } else {
             binding.progressBar.visibility = View.INVISIBLE
             binding.recyclerView.visibility = View.INVISIBLE
             binding.textNoItems.visibility = View.VISIBLE
             binding.textNumberItems.text = items.size.toString()
         }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "FavoriteDonationsFragment is onDestroyView")
        favoritesViewModel.detachListener()
        _binding = null
    }
}