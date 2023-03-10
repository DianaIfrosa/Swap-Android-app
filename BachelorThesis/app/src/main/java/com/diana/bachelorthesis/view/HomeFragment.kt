package com.diana.bachelorthesis.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ItemsRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentHomeBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.viewmodel.HomeViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    lateinit var homeViewModel: HomeViewModel
    private var displayExchangeItems: Boolean = true

    // TODO class adapter to acces recycler view component to manipulate the photo carousel
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textNumbeItems: TextView = binding.textNumberItems

        homeViewModel.donationItems.observe(viewLifecycleOwner) {
            updateRecyclerView()
        }

        homeViewModel.exchangeItems.observe(viewLifecycleOwner) {
            updateRecyclerView()
        }

        val switchMainCategories = binding.searchSwitchLayout.switchDonationExchange
        val switchDonation = binding.searchSwitchLayout.homeDonationSwitch
        val switchExchange = binding.searchSwitchLayout.homeExchangeSwitch

        switchMainCategories.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                displayExchangeItems = false
                updateRecyclerView()
                switchExchange.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                switchDonation.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            } else {
                displayExchangeItems = true
                updateRecyclerView()
                switchExchange.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                switchDonation.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            }
        }

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

//        if (screenWidth == 0 || screenHeight == 0) {
//            root.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
//                override fun onGlobalLayout() {
//                    view!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                    screenWidth = view!!.width
//                    screenHeight = view!!.height
//                    setChipsDimensions()
//                }
//            })
//        }

        // TODO, set custom layout based on the height width to the ui items

        return root
    }

    private fun updateRecyclerView() {
        if (displayExchangeItems) {
            binding.itemsAdapter =
                ItemsRecyclerViewAdapter(homeViewModel.exchangeItems.value!!, requireContext())
            binding.textNumberItems.text = homeViewModel.exchangeItems.value!!.size.toString()
        } else {
            binding.itemsAdapter =
                ItemsRecyclerViewAdapter(homeViewModel.donationItems.value!!, requireContext())
            binding.textNumberItems.text = homeViewModel.donationItems.value!!.size.toString()
        }
    }

//    fun setChipsDimensions() {
//        val layoutParamsExchangeChip = binding.homeExchangeButton.layoutParams
//        //layoutParamsExchangeChip?.setMargins(((1/20) * screenWidth).toInt(), 0, 0 , 0)
//        layoutParamsExchangeChip?.width = ((6.0/10.0) * screenWidth).toInt()
//        binding.homeExchangeButton.layoutParams = layoutParamsExchangeChip
//
//        val layoutParamsDonationChip = binding.homeDonationButton.layoutParams
//        layoutParamsDonationChip?.width = ((6.0/10.0) * screenWidth).toInt()
//        binding.homeDonationButton.layoutParams = layoutParamsDonationChip
//
////        val marginLayoutParamsDonation = binding.homeDonationButton.layoutParams as MarginLayoutParams
////        marginLayoutParamsDonation.setMargins( -((1.0/10.0) * screenWidth).toInt() , 0, 0, 0)
////        binding.homeDonationButton.layoutParams = marginLayoutParamsDonation
//
//    }

    fun getScreenPixelDensity(pixels: Float): Float {
        val screenPixelDensity = requireContext().resources.displayMetrics.density
        val dpValue = pixels / screenPixelDensity
        return dpValue
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}