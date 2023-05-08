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
import com.diana.bachelorthesis.adapters.FragmentPageAdapter
import com.diana.bachelorthesis.databinding.FragmentFavoritesBinding
import com.diana.bachelorthesis.utils.BasicFragment
import com.diana.bachelorthesis.viewmodel.FavoritesViewModel
import com.google.android.material.tabs.TabLayout

class FavoritesFragment : Fragment(), BasicFragment {

    private val TAG: String = FavoritesFragment::class.java.name
    private var _binding: FragmentFavoritesBinding? = null

    private val binding get() = _binding!!
    private lateinit var adapterFragment: FragmentPageAdapter
    private lateinit var favoritesViewModel: FavoritesViewModel

    private lateinit var smoothScroller: RecyclerView.SmoothScroller


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "FavoritesFragment is onCreateView")
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        initTabLayoutAndViewPager()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "FavoritesFragment is onViewCreated")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "FavoritesFragment is onActivityCreated")
        smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        favoritesViewModel = ViewModelProvider(this)[FavoritesViewModel::class.java]
        setMainPageAppbar(requireActivity(), requireView().findNavController().currentDestination!!.label.toString())
    }

    private fun initTabLayoutAndViewPager() {
        adapterFragment = FragmentPageAdapter(childFragmentManager, lifecycle)
        binding.viewPager.adapter = adapterFragment

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(resources.getString(R.string.exchanges)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(resources.getString(R.string.donations)))

        binding.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    binding.viewPager.currentItem = tab.position
                    favoritesViewModel.lastScrollPosition = 0
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
            "Restored recycler scroll position ${favoritesViewModel.lastScrollPosition}"
        )
        try {
        smoothScroller.targetPosition = favoritesViewModel.lastScrollPosition
        recyclerView.layoutManager!!.startSmoothScroll(smoothScroller)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Cannot scroll recycler view because target position is incorrect.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "FavoritesFragment is onDestroyView")
        _binding = null
        favoritesViewModel.detachListeners()
    }

    override fun initListeners() {

    }


}