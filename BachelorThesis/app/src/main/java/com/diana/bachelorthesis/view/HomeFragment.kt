package com.diana.bachelorthesis.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.adapters.ItemsRecyclerViewAdapter
import com.diana.bachelorthesis.databinding.FragmentHomeBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.utils.FilterDialogFragment
import com.diana.bachelorthesis.utils.LocationHelper
import com.diana.bachelorthesis.utils.SortDialogFragment
import com.diana.bachelorthesis.utils.SortFilterDialogListener
import com.diana.bachelorthesis.viewmodel.ItemsViewModel
import com.google.android.material.snackbar.Snackbar


class HomeFragment : Fragment(), SortFilterDialogListener {
    private val TAG: String = HomeFragment::class.java.name
    private var _binding: FragmentHomeBinding? = null
    lateinit var itemsViewModel: ItemsViewModel

    // todo add logo in app bar for home screen and name of the page for the other pages
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "HomeFragment is onCreateView")
        val viewModelFactory = ItemsViewModel.ViewModelFactory(LocationHelper(requireActivity().applicationContext))
        itemsViewModel = ViewModelProvider(this, viewModelFactory).get(ItemsViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.searchSwitchLayout.switchDonationExchange.isChecked = false
        val root: View = binding.root

        binding.searchSwitchLayout.searchView.clearFocus()

        updateRecyclerView(arrayListOf(), true)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "HomeFragment is onActivityCreated")
        setAppbar()
    }

    private fun setAppbar() {

        activity?.findViewById<ImageView>(R.id.logoApp)?.apply {
            Log.d(TAG, "applied")
            visibility = View.VISIBLE
        }
        activity?.findViewById<TextView>(R.id.titleAppBar)?.apply {
            Log.d(TAG, "applied")
            visibility = View.GONE
        }
    }

    private fun initListeners() {
        // FIXME is it really ok this choice of live data?

        itemsViewModel.donationItems.observe(viewLifecycleOwner) {
            if (!itemsViewModel.displayExchangeItems) {
                updateRecyclerView(itemsViewModel.currentItems, itemsViewModel.currentItems.isEmpty())
            }
        }

        itemsViewModel.exchangeItems.observe(viewLifecycleOwner) {
            if (itemsViewModel.displayExchangeItems)
                updateRecyclerView(itemsViewModel.currentItems, itemsViewModel.currentItems.isEmpty())
        }

       initSwitchCategoriesListener()

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Redirect to addItemFragment", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

//            val addItemFragment = AddItemFragment()
//            val transaction = activity?.supportFragmentManager?.beginTransaction()
//            if (transaction != null) {
//                transaction.replace(R.id.nav_add_item, addItemFragment)
//                transaction.addToBackStack(null)
//                transaction.commit()
//            }
        }

        binding.searchSwitchLayout.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(inputText: String?): Boolean {
                if (! inputText.isNullOrEmpty()) {
                    itemsViewModel.restoreDefaultCurrentItems()

                    itemsViewModel.searchItem(inputText)
                    updateRecyclerView(itemsViewModel.currentItems)
                }
                return false
            }

            override fun onQueryTextChange(inputText: String?): Boolean {
                if (inputText.isNullOrEmpty()) {
                    itemsViewModel.restoreDefaultCurrentItems()

                    updateRecyclerView(itemsViewModel.currentItems)
                }
                return false
            }

        })

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

    private fun initSwitchCategoriesListener() {
        val switchMainCategories = binding.searchSwitchLayout.switchDonationExchange
        val switchDonation = binding.searchSwitchLayout.homeDonationSwitch
        val switchExchange = binding.searchSwitchLayout.homeExchangeSwitch

        switchMainCategories.setOnCheckedChangeListener { _, checked ->
            Log.d(TAG, "Switch clicked")
            itemsViewModel.displayExchangeItems = !checked

            // clear search bar text
            binding.searchSwitchLayout.searchView.setQuery("", false)
            binding.searchSwitchLayout.searchView.clearFocus()

            itemsViewModel.restoreDefaultCurrentItems()
            updateRecyclerView(itemsViewModel.currentItems)

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
            binding.homeRecyclerView.visibility = View.INVISIBLE
        } else {
            binding.homeRecyclerView.visibility = View.VISIBLE
            binding.progressBarHome.visibility = View.INVISIBLE
            binding.itemsAdapter =
                ItemsRecyclerViewAdapter(items, requireContext())
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

    // TODO add onPause to restore data with bundle?
    override fun saveSortOption(option: Int) {
        itemsViewModel.applySort(option)
        updateRecyclerView(itemsViewModel.currentItems)
    }

    override fun saveFilterOptions(city: String, categories: List<ItemCategory>) {
        itemsViewModel.applyFilter(city, categories)
        updateRecyclerView(itemsViewModel.currentItems)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // TODO uncomment this: saveDefaultOptions()
        _binding = null
        itemsViewModel.detachListeners()
        Log.d(TAG, "HomeFragment is onDestroyView")
    }
}
