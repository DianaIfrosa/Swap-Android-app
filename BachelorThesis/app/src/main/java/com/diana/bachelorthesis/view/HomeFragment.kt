package com.diana.bachelorthesis.view

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
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
import com.diana.bachelorthesis.utils.*
import com.diana.bachelorthesis.viewmodel.ItemsViewModel
import com.diana.bachelorthesis.viewmodel.UserViewModel

class HomeFragment : Fragment(), SortFilterDialogListener, BasicFragment {
    private val TAG: String = HomeFragment::class.java.name

    private var _binding: FragmentHomeBinding? = null
    private var firstVisiblePosition = 0
    lateinit var itemsViewModel: ItemsViewModel
    lateinit var userViewModel: UserViewModel
    lateinit var sharedPref: SharedPreferences

    private val binding get() = _binding!!
    private lateinit var smoothScroller: RecyclerView.SmoothScroller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "HomeFragment is onCreate")
    }

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
        val viewModelFactory =
            ItemsViewModel.ViewModelFactory(LocationHelper(requireActivity().applicationContext))
        itemsViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ItemsViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "HomeFragment is onActivityCreated")

        smoothScroller = object : LinearSmoothScroller(context) {
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
        itemsViewModel.populateLiveData()

        setHomeAppbar(requireActivity())
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
        setSearchBarUI()
        Log.d(TAG, "HomeFragment is onStart")
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
                categoriesList.add(ItemCategory.stringToItemCategory(categoryName))
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

            itemsViewModel.restoreDefaultCurrentItems()
            updateRecyclerView(itemsViewModel.currentItems)
            scrollRecyclerView()

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

    fun updateRecyclerView(items: List<Item>, progressBarAppears: Boolean = false) {
        if (progressBarAppears) {
            binding.progressBarHome.visibility = View.VISIBLE
            binding.textNumberItems.text = resources.getString(R.string.loading)
            binding.homeRecyclerView.visibility = View.INVISIBLE
        } else {
            binding.homeRecyclerView.visibility = View.VISIBLE
            binding.progressBarHome.visibility = View.INVISIBLE
            binding.itemsAdapter =
                ItemsRecyclerViewAdapter(items, requireContext()) { item ->
                    (requireActivity() as MainActivity).returnedHomeFromItemPage = true
                    val action = HomeFragmentDirections.actionNavHomeToNavItem(item)
                    requireView().findNavController().navigate(action)
                }
            binding.textNumberItems.text = items.size.toString()
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

    override fun saveFilterOptions(city: String, categories: List<ItemCategory>) {
        itemsViewModel.applyFilter(city, categories)
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
