package com.diana.bachelorthesis.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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

    // TODO class adapter to acces recycler view component to manipulate the photo carousel
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        populateData()

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

        val switchMainCategories = binding.searchSwitchLayout.switchDonationExchange
        val switchDonation = binding.searchSwitchLayout.homeDonationSwitch
        val switchExchange = binding.searchSwitchLayout.homeExchangeSwitch

        switchMainCategories.setOnCheckedChangeListener { _, checked ->

            if (checked) {
                switchExchange.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                switchDonation.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            } else {
                switchExchange.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                switchDonation.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            }
        }

        // TODO, set custom layout based on the height width to the ui items

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        return root
    }

    fun populateData() {
        val itemsList: ArrayList<Item> = arrayListOf()
        itemsList.add(
            Item(
                "email@gmail.com",
                ItemCategory.DEVICE,
                "description",
                GeoPoint(80.0, 80.0),
                "name",
                arrayListOf(),
                Timestamp(85939530, 848),
                null
            )
        )
        itemsList.add(
            Item(
                "email2@gmail.com",
                ItemCategory.DEVICE,
                "description2",
                GeoPoint(80.0, 80.0),
                "name2",
                arrayListOf(),
                Timestamp(85939530, 848),
                null
            )
        )
        itemsList.add(
            Item(
                "email3@gmail.com",
                ItemCategory.DEVICE,
                "description3",
                GeoPoint(80.0, 80.0),
                "name3",
                arrayListOf(),
                Timestamp(85939530, 848),
                null
            )
        )
        itemsList.add(
            Item(
                "email3@gmail.com",
                ItemCategory.DEVICE,
                "description4",
                GeoPoint(80.0, 80.0),
                "name4",
                arrayListOf(),
                Timestamp(85939530, 848),
                null
            )
        )
        itemsList.add(
            Item(
                "email3@gmail.com",
                ItemCategory.DEVICE,
                "description5",
                GeoPoint(80.0, 80.0),
                "name5",
                arrayListOf(),
                Timestamp(85939530, 848),
                null
            )
        )

        binding.itemsAdapter = ItemsRecyclerViewAdapter(itemsList, requireContext())
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