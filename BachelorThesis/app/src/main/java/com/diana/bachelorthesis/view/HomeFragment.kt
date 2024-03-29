package com.diana.bachelorthesis.view

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ItemsRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentHomeBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.model.ItemDonation
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.utils.*
import com.diana.bachelorthesis.viewmodel.ItemsViewModel
import com.diana.bachelorthesis.viewmodel.UserViewModel

class HomeFragment : Fragment(), SortFilterDialogListener, BasicFragment {
    private val TAG: String = HomeFragment::class.java.name

    private var _binding: FragmentHomeBinding? = null
    private lateinit var itemsViewModel: ItemsViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var sharedPref: SharedPreferences

    private val binding get() = _binding!!
    private lateinit var smoothScroller: RecyclerView.SmoothScroller

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "HomeFragment is onCreateView")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
//        binding.searchSwitchLayout.switchDonationExchange.isChecked = false
        val root: View = binding.root
        updateRecyclerViewSpan()
        return root
    }

    private fun updateRecyclerViewSpan() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.homeRecyclerView.layoutManager = GridLayoutManager(requireActivity(), 1)
        } else {
            binding.homeRecyclerView.layoutManager = GridLayoutManager(requireActivity(), 2)
        }
    }

    private fun getViewModels() {
        itemsViewModel = ViewModelProvider(requireActivity())[ItemsViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "HomeFragment is onViewCreated")
        setHomeAppbar(requireActivity())
        smoothScroller = object : LinearSmoothScroller(requireActivity()) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        getViewModels()

//        restoreSharedPreferencesData()

        if (itemsViewModel.currentItems.isNotEmpty()) {
            updateRecyclerView(itemsViewModel.currentItems)
        } else {
            updateRecyclerView(arrayListOf(), true)
        }

//        // restore search bar text
//        binding.searchSwitchLayout.searchEditText.isActivated = true
//        binding.searchSwitchLayout.searchEditText.onActionViewExpanded()
//        binding.searchSwitchLayout.searchView.isIconified = false
//        binding.searchSwitchLayout.searchView.clearFocus()
//        binding.searchSwitchLayout.searchView.setQuery("text", false)

        initListeners()
        itemsViewModel.currentUser = (requireActivity() as MainActivity).getCurrentUser()
        itemsViewModel.populateLiveData(requireActivity())

    }

    private fun setSearchBarUI() {
        binding.searchSwitchLayout.searchEditText.setText(itemsViewModel.searchText)
        if (itemsViewModel.searchText.isNotEmpty()) {
            binding.searchSwitchLayout.btnClearSearch.visibility = View.VISIBLE
        } else {
            binding.searchSwitchLayout.btnClearSearch.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "HomeFragment is onStart")
        setSearchBarUI()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "HomeFragment is onResume")
    }


    private fun restoreSharedPreferencesData() {
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)

        val categoriesString: String =
            sharedPref.getString(SharedPreferencesUtils.sharedPrefCategoriesFilter, "") ?: ""
        val categoriesList: MutableList<ItemCategory> = mutableListOf()
        if (categoriesString.isNotEmpty()) {
            categoriesString.split(",").forEach { categoryName ->
                categoriesList.add(ItemCategory.stringToItemCategory(requireActivity(), categoryName))
            }
        }

        itemsViewModel.setSearchFilterSortOptions(
            sharedPref.getString(SharedPreferencesUtils.sharedprefSearch, "") ?: "",
            sharedPref.getInt(SharedPreferencesUtils.sharedprefSortOption, 0),
            sharedPref.getString(SharedPreferencesUtils.sharedprefCityFilter, "") ?: "",
            categoriesList
        )

    }

    override fun initListeners() {
        // FIXME is it really ok this choice of live data?

        itemsViewModel.donationItems.observe(viewLifecycleOwner) {
            Log.d(TAG, "Observed change in donation items live data")
            if (!itemsViewModel.displayExchangeItems) {
                updateRecyclerView(
                    itemsViewModel.currentItems
                )
                scrollRecyclerView()
            }
        }

        itemsViewModel.exchangeItems.observe(viewLifecycleOwner) {
            Log.d(TAG, "Observed change in exchange items live data")
            if (itemsViewModel.displayExchangeItems) {
                updateRecyclerView(
                    itemsViewModel.currentItems
                )
                scrollRecyclerView()
            }

            initSwitchCategoriesListener()

            binding.addItemButton.setOnClickListener {
                if (userViewModel.verifyUserLoggedIn()) {
                    // Redirect to add item page
                    // Chose to use this approach because it is not buggy in terms of back stack pop
                    val item =
                        (requireActivity() as MainActivity).navView.findViewById<View>(R.id.nav_add_item)
                    item.callOnClick()
                } else {
                    // Redirect to auth page
                    requireView().findNavController().navigate(R.id.nav_intro_auth)
                }
            }

            binding.searchSwitchLayout.searchEditText.setOnEditorActionListener { textView, actionId, keyEvent ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val inputText = textView.text.toString()
                    if (inputText.isNotEmpty()) {
                        itemsViewModel.restoreDefaultCurrentItemsAndOptions()
                        itemsViewModel.searchItem(inputText)
                        updateRecyclerView(itemsViewModel.currentItems)
                    } else {
                        itemsViewModel.restoreDefaultCurrentItemsAndOptions()
                        updateRecyclerView(itemsViewModel.currentItems)
                    }
                    false
                } else false
            }

            binding.searchSwitchLayout.searchEditText.addTextChangedListener {
                if (it.toString().isNotEmpty()) {
                    binding.searchSwitchLayout.btnClearSearch.visibility = View.VISIBLE
                } else {
                    binding.searchSwitchLayout.btnClearSearch.visibility = View.GONE
                }
            }

            binding.searchSwitchLayout.btnClearSearch.setOnClickListener {
                itemsViewModel.restoreDefaultCurrentItemsAndOptions()
                binding.searchSwitchLayout.searchEditText.setText(itemsViewModel.searchText)
                updateRecyclerView(itemsViewModel.currentItems)
                binding.searchSwitchLayout.btnClearSearch.visibility = View.GONE
            }

            binding.buttonSort.setOnClickListener {
                val sortDialogFragment = SortDialogFragment()
                sortDialogFragment.isCancelable = false

                val bundle = Bundle()
                bundle.putInt("sortOption", itemsViewModel.sortOption)
                sortDialogFragment.arguments = bundle

                sortDialogFragment.show(childFragmentManager, "SortDialogFragment")
            }

            binding.buttonFilter.setOnClickListener {
                val filterDialogFragment = FilterDialogFragment()
                filterDialogFragment.isCancelable = false

                val bundle = itemsViewModel.getFilterBundle()

                filterDialogFragment.arguments = bundle

                filterDialogFragment.show(childFragmentManager, "FilterDialogFragment")
            }
        }
    }

    private fun scrollRecyclerView() {
        Log.d(
            TAG,
            "Restored recycler scroll position ${itemsViewModel.lastScrollPosition}"
        )
        try {
            smoothScroller.targetPosition = itemsViewModel.lastScrollPosition
            binding.homeRecyclerView.layoutManager!!.startSmoothScroll(smoothScroller)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Cannot scroll recycler view because because target position is incorrect.")
        }
    }

    private fun initSwitchCategoriesListener() {
        val switchMainCategories = binding.searchSwitchLayout.switchDonationExchange
        val switchDonation = binding.searchSwitchLayout.homeDonationSwitch
        val switchExchange = binding.searchSwitchLayout.homeExchangeSwitch

        switchMainCategories.setOnCheckedChangeListener { _, checked ->
            Log.d(TAG, "Switch clicked in HomeFragment")
            itemsViewModel.displayExchangeItems = !checked
            itemsViewModel.lastScrollPosition = 0

            itemsViewModel.restoreDefaultCurrentItems(requireActivity())
            updateRecyclerView(itemsViewModel.currentItems)
            scrollRecyclerView()

            if (checked) {
                switchExchange.setTextColor(ContextCompat.getColor(requireActivity(), R.color.grey))
                switchDonation.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
            } else {
                switchExchange.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                switchDonation.setTextColor(ContextCompat.getColor(requireActivity(), R.color.grey))
            }
        }
    }

    fun updateRecyclerView(items: List<Item>, progressBarAppears: Boolean = false) {
        if (progressBarAppears) {
            binding.progressBarHome.visibility = View.VISIBLE
            binding.textNumberItems.text = resources.getString(R.string.loading)
            binding.homeRecyclerView.visibility = View.INVISIBLE
            binding.noItemsText.visibility = View.INVISIBLE
        } else if (items.isNotEmpty()) {
            binding.homeRecyclerView.visibility = View.VISIBLE
            binding.progressBarHome.visibility = View.INVISIBLE
            binding.noItemsText.visibility = View.INVISIBLE
            binding.itemsAdapter =
                ItemsRecyclerViewAdapter(items, requireContext()) { item ->
                    if (item is ItemExchange) {
                        val newItem = item.clone()
                        val action =
                            HomeFragmentDirections.actionNavHomeToNavItem(newItem)
                        requireView().findNavController().navigate(action)
                    } else if (item is ItemDonation) {
                        val newItem = item.clone()
                        val action =
                            HomeFragmentDirections.actionNavHomeToNavItem(newItem)
                        requireView().findNavController().navigate(action)
                    }
                }
            binding.textNumberItems.text = items.size.toString() + " " + resources.getString(R.string.items)
        } else {
            binding.homeRecyclerView.visibility = View.INVISIBLE
            binding.progressBarHome.visibility = View.INVISIBLE
            binding.noItemsText.visibility = View.VISIBLE
            binding.textNumberItems.text = "0 " + resources.getString(R.string.items)
        }
    }

//    private fun saveDefaultOptions() {
//
//        // FIXME duplicated code ugly
//        if (!itemsViewModel.displayExchangeItems) {
//            binding.searchSwitchLayout.switchDonationExchange.performClick()
//        } else {
//            // clear search bar text
//            binding.searchSwitchLayout.searchView.setQuery("", false)
//            binding.searchSwitchLayout.searchView.clearFocus()
//            itemsViewModel.restoreDefaultCurrentItems()
//        }
//    }

    override fun saveSortOption(option: Int) {
        itemsViewModel.applySort(option)
        updateRecyclerView(itemsViewModel.currentItems)
    }

    override fun saveFilterOptions(context: Context, city: String, categories: List<ItemCategory>) {
        itemsViewModel.applyFilter(context, city, categories)
        updateRecyclerView(itemsViewModel.currentItems)
    }

    private fun writeSharedPreferencesData() {
        with(sharedPref.edit()) {
            putString(SharedPreferencesUtils.sharedprefSearch, itemsViewModel.searchText)
            putInt(SharedPreferencesUtils.sharedprefSortOption, itemsViewModel.sortOption)
            putString(SharedPreferencesUtils.sharedprefCityFilter, itemsViewModel.cityFilter)
            putString(
                SharedPreferencesUtils.sharedPrefCategoriesFilter,
                itemsViewModel.categoriesFilter.joinToString(",")
            )
            apply() // asynchronously
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "HomeFragment is onPause")
        itemsViewModel.lastScrollPosition =
            (binding.homeRecyclerView.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "HomeFragment is onStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "HomeFragment is onDestroyView")
        _binding = null
//        writeSharedPreferencesData()
        itemsViewModel.detachListeners()
    }
}
