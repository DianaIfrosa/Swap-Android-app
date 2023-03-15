package com.diana.bachelorthesis.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ItemsRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentHomeBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.utils.FilterDialogFragment
import com.diana.bachelorthesis.utils.SortDialogFragment
import com.diana.bachelorthesis.viewmodel.HomeViewModel
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {
    private val TAG: String = HomeFragment::class.java.name
    private val HOME_FRAGMENT: Int = 0
    private var _binding: FragmentHomeBinding? = null
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    lateinit var homeViewModel: HomeViewModel

    // TODO class adapter to acces recycler view component to manipulate the photo carousel
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var sharedPreferences : SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "HomeFragment is onCreateView")
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.searchSwitchLayout.switchDonationExchange.isChecked = false
        val root: View = binding.root

        initListeners()

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


    private fun initListeners() {
        // FIXME is it really ok this choice of live data?

        homeViewModel.donationItems.observe(viewLifecycleOwner) {
            if (homeViewModel.displayExchangeItems)
                updateRecyclerView(homeViewModel.currentItems)
        }

        homeViewModel.exchangeItems.observe(viewLifecycleOwner) {
            if (homeViewModel.displayExchangeItems)
                updateRecyclerView(homeViewModel.currentItems)
        }

       initSwitchCategoriesListener()

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        binding.searchSwitchLayout.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(inputText: String?): Boolean {
                if (! inputText.isNullOrEmpty()) {
                    homeViewModel.restoreDefaultCurrentItems()
                    saveSortOption(0)

                    homeViewModel.searchItem(inputText)
                    updateRecyclerView(homeViewModel.currentItems)
                }
                return false
            }

            override fun onQueryTextChange(inputText: String?): Boolean {
                if (inputText.isNullOrEmpty()) {
                    homeViewModel.restoreDefaultCurrentItems()
                    saveSortOption(0)

                    updateRecyclerView(homeViewModel.currentItems)
                }
                return false
            }

        })

        binding.buttonSort.setOnClickListener {
            val sortDialogFragment = SortDialogFragment()
            sortDialogFragment.isCancelable = false
            sortDialogFragment.show(childFragmentManager, "SortDialogFragment")
        }

        binding.buttonFilter.setOnClickListener {
            val filterDialogFragment = FilterDialogFragment()
            filterDialogFragment.isCancelable = false
            filterDialogFragment.show(childFragmentManager, "FilterDialogFragment")
        }

    }

    private fun initSwitchCategoriesListener() {
        val switchMainCategories = binding.searchSwitchLayout.switchDonationExchange
        val switchDonation = binding.searchSwitchLayout.homeDonationSwitch
        val switchExchange = binding.searchSwitchLayout.homeExchangeSwitch

        switchMainCategories.setOnCheckedChangeListener { _, checked ->
            Log.d(TAG, "Switch clicked")
            homeViewModel.displayExchangeItems = !checked

            // clear search bar text
            binding.searchSwitchLayout.searchView.setQuery("", false)
            binding.searchSwitchLayout.searchView.clearFocus()

            homeViewModel.restoreDefaultCurrentItems()
            saveSortOption(0)
            updateRecyclerView(homeViewModel.currentItems)

            if (checked) {
                switchExchange.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                switchDonation.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                // TODO change background color accordingly
            } else {
                switchExchange.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                switchDonation.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                // TODO change background color accordingly
            }
        }
    }

    fun updateRecyclerView(items: List<Item>) {
        Log.d(TAG, "Recycler view updated")
        binding.itemsAdapter =
            ItemsRecyclerViewAdapter(items, requireContext())
        binding.textNumberItems.text = items.size.toString()
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

//    }


//    private fun showSortAlertDialog() {
//        val sortAlertDialog = AlertDialog.Builder(requireContext())
//        sortAlertDialog.setTitle(getResources().getString(R.string.sort))
//        val items: List<String> = arrayListOf()
//        sortAlertDialog.setSingleChoiceItems(resources.getStringArray(R.array.sort_options),
//            1,
//        )
//        sortAlertDialog.create().show()
//    }


    fun getScreenPixelDensity(pixels: Float): Float {
        val screenPixelDensity = requireContext().resources.displayMetrics.density
        val dpValue = pixels / screenPixelDensity
        return dpValue
    }

    fun saveSortOption(option: Int) {
        sharedPreferences = this.activity?.getSharedPreferences("SHARED_PREF_HOME", Context.MODE_PRIVATE)
        if (sharedPreferences != null) {
            val editor = sharedPreferences!!.edit()
            editor.putInt("sortOption", option)
            editor.apply()
        }

        // FIXME: might crash when trying to reuse this fragment in another fragment (e.g. sort dialog in recommendations fragment)
        // but I might as well not add sort/filter there
    }

    fun getSavedSortOption(): Int {
        var option = 0
        sharedPreferences = this.activity?.getSharedPreferences("SHARED_PREF_HOME", Context.MODE_PRIVATE)
        if (sharedPreferences != null) {
            option = sharedPreferences!!.getInt("sortOption", 0)
        }

        return option
    }

    fun saveDefaultOptions() {

        // FIXME duplicated code ugly
        if (!homeViewModel.displayExchangeItems) {
            binding.searchSwitchLayout.switchDonationExchange.performClick()
        } else {
            // clear search bar text
            binding.searchSwitchLayout.searchView.setQuery("", false)
            binding.searchSwitchLayout.searchView.clearFocus()
            homeViewModel.restoreDefaultCurrentItems()
            saveSortOption(0)
        }

        // TODO add for filter in sharedpref
    }

    // TODO add onPause to restore data with bundle?

    override fun onStop() {
        super.onStop()
        saveDefaultOptions()
        _binding = null
        Log.d(TAG, "HomeFragment is destroyed")
    }
}