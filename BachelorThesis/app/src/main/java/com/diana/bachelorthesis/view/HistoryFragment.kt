package com.diana.bachelorthesis.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.HistoryPagesAdapter
import com.diana.bachelorthesis.databinding.FragmentHistoryBinding
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.viewmodel.HistoryViewModel
import com.diana.bachelorthesis.viewmodel.ItemsViewModel
import com.google.android.material.tabs.TabLayout

class HistoryFragment : Fragment(), BasicFragment {

    private val TAG: String = HistoryFragment::class.java.name
    private var _binding: FragmentHistoryBinding? = null
    private lateinit var adapterPages: HistoryPagesAdapter

    private val binding get() = _binding!!
    private lateinit var smoothScroller: RecyclerView.SmoothScroller
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var itemsViewModel: ItemsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "HistoryFragment is onCreateView")
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initTabLayoutAndViewPager()

        return root
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "HistoryFragment is onStart")
    }

    private fun initTabLayoutAndViewPager() {
        adapterPages = HistoryPagesAdapter(childFragmentManager, lifecycle)
        binding.viewPager.adapter = adapterPages

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(resources.getString(R.string.available)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(resources.getString(R.string.not_available)))

        binding.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    binding.viewPager.currentItem = tab.position
                    historyViewModel.lastScrollPosition = 0
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        binding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })
    }

    fun scrollRecyclerView(recyclerView: RecyclerView) {
        Log.d(
            TAG,
            "Restored recycler scroll position ${historyViewModel.lastScrollPosition}"
        )
        try {
            smoothScroller.targetPosition = historyViewModel.lastScrollPosition
            recyclerView.layoutManager!!.startSmoothScroll(smoothScroller)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Cannot scroll recycler view because target position is incorrect.")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "HistoryFragment is onActivityCreated")
        smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }

        getViewModels()
        historyViewModel.currentUser = (requireActivity() as MainActivity).getCurrentUser()!!
        setMainPageAppbar(requireActivity(), requireView().findNavController().currentDestination!!.label.toString())
    }

    private fun getViewModels() {
        historyViewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
        itemsViewModel = ViewModelProvider(requireActivity())[ItemsViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "HistoryFragment is onDestroyView")
        _binding = null
    }

    override fun initListeners() {
    }

}