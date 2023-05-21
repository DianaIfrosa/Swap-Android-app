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
import com.diana.bachelorthesis.databinding.FragmentFavoriteExchangesBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.viewmodel.FavoriteExchangesViewModel
import com.diana.bachelorthesis.viewmodel.FavoritesViewModel

class FavoriteExchangesFragment : Fragment() {
    private val TAG: String = FavoriteExchangesFragment::class.java.name
    private var _binding: FragmentFavoriteExchangesBinding? = null

    private val binding get() = _binding!!
    private lateinit var favoritesViewModel: FavoriteExchangesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "FavoriteExchangesFragment is onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "FavoriteExchangesFragment is onCreateView")
        _binding = FragmentFavoriteExchangesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.itemsAdapter = ItemsRecyclerViewAdapter(listOf(), requireContext()) {}
        updateRecyclerView(listOf(), true)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "FavoriteExchangesFragment is onViewCreated")

        favoritesViewModel = ViewModelProvider(this)[FavoriteExchangesViewModel::class.java]
        favoritesViewModel.favoriteExchangesIds = (requireActivity() as MainActivity).getCurrentUser()!!.favoriteExchanges
        favoritesViewModel.populateLiveDataExchanges()

        favoritesViewModel.exchangeItems.observe(viewLifecycleOwner) { items->
            Log.d(TAG, "Observer called")
            updateRecyclerView(items)
//            (parentFragment as FavoritesFragment).scrollRecyclerView(binding.recyclerView)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "FavoriteExchangesFragment is onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "FavoriteExchangesFragment is onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "FavoriteExchangesFragment is onPause")
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
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "FavoriteExchangesFragment is onStop")
//        favoritesViewModel.lastScrollPosition =
//            (binding.recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "FavoriteExchangesFragment is onDestroyView")
        favoritesViewModel.detachListener()
        _binding = null
    }
}