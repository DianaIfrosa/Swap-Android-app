package com.diana.bachelorthesis.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ItemsRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentRecommendationsBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.viewmodel.MainViewModel
import com.diana.bachelorthesis.viewmodel.RecommendationsViewModel

class RecommendationsFragment : Fragment(), BasicFragment {

    private val TAG: String = RecommendationsFragment::class.java.name
    private var _binding: FragmentRecommendationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var recommendationsViewModel: RecommendationsViewModel
    private lateinit var mainViewModel: MainViewModel

    private lateinit var smoothScroller: RecyclerView.SmoothScroller

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecommendationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "RecommendationsFragment is onViewCreated")
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.itemsAdapter = ItemsRecyclerViewAdapter(listOf(), requireContext()) {}
    }

    private fun scrollRecyclerView() {
        Log.d(
            TAG,
            "Restored recycler scroll position ${recommendationsViewModel.lastScrollPosition}"
        )
        try {
            smoothScroller.targetPosition = recommendationsViewModel.lastScrollPosition
            binding.recyclerView.layoutManager!!.startSmoothScroll(smoothScroller)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Cannot scroll recycler view because target position is incorrect.")
        }
    }

    private fun getViewModels() {
        recommendationsViewModel = ViewModelProvider(requireActivity())[RecommendationsViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        recommendationsViewModel.currentUser = mainViewModel.currentUser!!
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "RecommendationsFragment is onActivityCreated")
        getViewModels()
        updateRecyclerView(listOf(), true)
        setMainPageAppbar(requireActivity(), requireView().findNavController().currentDestination!!.label.toString())
        if (mainViewModel.clickedOnRecommendations) {
            recommendationsViewModel.lastScrollPosition = 0
            initListeners()
            recommendationsViewModel.populateLiveDataItems()
        } else if (mainViewModel.modifiedRecommendations) {
            // the user modified the preferences from the profile
            recommendationsViewModel.lastScrollPosition = 0
            initListeners()
            recommendationsViewModel.populateLiveDataItems()
            mainViewModel.modifiedRecommendations = false
        } else {
            recommendationsViewModel.items.value?.let { updateRecyclerView(it) }
            scrollRecyclerView()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "RecommendationsFragment is onResume")
    }

    override fun initListeners() {
        recommendationsViewModel.items.observe(viewLifecycleOwner) {
            updateRecyclerView(it)
        }
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
                    val action = RecommendationsFragmentDirections.actionNavRecommendationsToNavItem(item)
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
        Log.d(TAG, "RecommendationsFragment is onStop")
        recommendationsViewModel.lastScrollPosition =
            (binding.recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "RecommendationsFragment is onDestroyView")
        mainViewModel.clickedOnRecommendations = false
        _binding = null
    }

}