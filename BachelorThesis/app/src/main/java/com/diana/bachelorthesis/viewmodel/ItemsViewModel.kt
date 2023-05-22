package com.diana.bachelorthesis.viewmodel

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import com.diana.bachelorthesis.model.*
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.repository.ItemRepository
import kotlin.collections.ArrayList

class ItemsViewModel : ViewModel() {
    private val TAG: String = ItemsViewModel::class.java.name
    private val itemRepository = ItemRepository.getInstance()

    private val _exchangeItems = MutableLiveData<List<Item>>()
    private val _donationItems = MutableLiveData<List<Item>>()

    var exchangeItems: LiveData<List<Item>> = _exchangeItems
    val donationItems: LiveData<List<Item>> = _donationItems

    var currentItems: ArrayList<Item> = arrayListOf()
    var displayExchangeItems: Boolean = true

    var searchText: String = ""
    var sortOption: Int = 0
    var cityFilter: String = ""
    var categoriesFilter: List<ItemCategory> = arrayListOf()

    var currentUser: User? = null

    var lastScrollPosition = 0

     fun populateLiveData(context: Context) {
        //_exchangeItems.value = repository.getItems(true)
        //_donationItems.value = repository.getItems(false)

        Log.d(TAG, "Populate live data in itemsViewModel")
        itemRepository.getExchangeItemsAndListen(object : ListParamCallback<Item> {
            override fun onComplete(values: ArrayList<Item>) {
                val valuesSorted = ArrayList(values.sortedByDescending { item -> item.postDate }) // new to old
                val valuesSortedAndFiltered = ArrayList(valuesSorted.filter { item ->
                    item as ItemExchange
                    (item.exchangeInfo == null) && (currentUser?.email != item.owner)
                })
                if (displayExchangeItems) {
                    updateCurrentItems(context, valuesSortedAndFiltered)
                }
                _exchangeItems.value = valuesSortedAndFiltered
                Log.d(TAG, "Updated exchange items")

//                viewModelScope.launch {
//                    _exchangeItems.value = locationHelper.getItemsCities(values)
//                    if (displayExchangeItems)
//                        updateCurrentItems(values)
//                }
            }

            override fun onError(e: Exception?) {}
        })

        itemRepository.getDonationItemsAndListen(object : ListParamCallback<Item> {
            override fun onComplete(values: ArrayList<Item>) {
                val valuesSorted = ArrayList(values.sortedByDescending { item -> item.postDate }) // new to old
                val valuesSortedAndFiltered = ArrayList(valuesSorted.filter { item ->
                    item as ItemDonation
                    (item.donationInfo == null) && (currentUser?.email != item.owner)
                })
                if (!displayExchangeItems) {
                    updateCurrentItems(context, valuesSortedAndFiltered)
                }
                _donationItems.value = valuesSortedAndFiltered
                Log.d(TAG, "Updated donation items")
//                viewModelScope.launch {
//                    _donationItems.value = locationHelper.getItemsCities(values)
//                    if (!displayExchangeItems)
//                        updateCurrentItems(values)
//                }
            }

            override fun onError(e: Exception?) {}
        })
    }

    fun updateCurrentItems(context: Context, items: ArrayList<Item>) {
        // called when there is a change in DB and the data must be updated
        // and the user's search, sort, filter preferences should be restored

        currentItems = items
        if (searchText.isNotEmpty()) {
            searchItem(searchText)
        }

        applySort(sortOption)
        applyFilter(context, cityFilter, categoriesFilter, currentItems)
    }

    private fun restoreDefaultOptions() {
        sortOption = 0
        searchText = ""
        cityFilter = ""
        categoriesFilter = arrayListOf()
    }

    fun setSearchFilterSortOptions(search: String, sort: Int, city: String, categories: List<ItemCategory>) {
        searchText = search
        sortOption = sort
        cityFilter = city
        categoriesFilter = categories
    }

    fun searchItem(inputText: String) {
        // modifies the currentItems value accordingly
        Log.d(TAG, "Search item by text $inputText")
        val result: ArrayList<Item> = arrayListOf()

        searchText = inputText
        val inputWords = inputText.split(" ")
        for (item in currentItems) {
            if (inputWords.any {
                    item.name.lowercase().contains(it.lowercase())
                } || inputWords.any {
                    item.description.lowercase().contains(it.lowercase())
                }) {
                result.add(item)
            }
        }

        currentItems = result
    }

    fun restoreDefaultCurrentItemsAndOptions() {
        // restore default sort option and remove filter options selected
        Log.d(TAG, "Restored default current items.")
        restoreDefaultOptions()

        currentItems = if (displayExchangeItems) _exchangeItems.value as ArrayList
        else _donationItems.value as ArrayList
    }

    fun restoreDefaultCurrentItems(context: Context) {
        // restore default sort option and remove filter options selected
        Log.d(TAG, "Restored default current items.")
//        restoreDefaultOptions()

        currentItems = if (displayExchangeItems) _exchangeItems.value as ArrayList
        else _donationItems.value as ArrayList
        updateCurrentItems(context, currentItems)
    }

    fun applySort(option: Int) {
        Log.d(TAG, "Sort by option $option")
        sortOption = option
        currentItems = sort(option, currentItems)
    }

    private fun sort(option: Int, items: ArrayList<Item>): ArrayList<Item> {
        val itemsTemp = when (option) {
            0 -> items.sortedByDescending { item -> item.postDate } // new to old
            1 -> items.sortedBy { item -> item.postDate } // old to new
            2 -> items.sortedBy { item -> item.name.lowercase() } // alphabetical A-Z
            3 -> items.sortedByDescending { item -> item.name.lowercase() } // alphabetical Z-A
            else -> {
                Log.w(TAG, "Invalid sorting option $option!")
                items
            }
        }
        return ArrayList(itemsTemp)
    }

    fun applyFilter(context: Context, city: String, categories: List<ItemCategory>, data: ArrayList<Item>? = null) {
        Log.d(
            TAG,
            "Filter by city $city and categories ${categories.joinToString { ItemCategory.getTranslatedName(context, it)}}"
        )
        cityFilter = city
        categoriesFilter = categories

        val rawData: ArrayList<Item>
        if (data != null) {
            rawData = data
        } else {
            rawData = if (displayExchangeItems) _exchangeItems.value as ArrayList
            else _donationItems.value as ArrayList
        }

        // apply sort on raw data then filter, to manage check and uncheck situations
        currentItems = filter(city, categories, sort(sortOption, rawData))
    }

    private fun filter(
        city: String,
        categories: List<ItemCategory>,
        items: ArrayList<Item>
    ): ArrayList<Item> {
        var itemsTemp: ArrayList<Item> = items

        if (city.isNotEmpty()) {
            itemsTemp = ArrayList(itemsTemp.filter { item -> item.city == city })
        }

        if (categories.isNotEmpty()) {
            itemsTemp = ArrayList(itemsTemp.filter { item -> item.category in categories })
        }

        return itemsTemp
    }

    private fun getCurrentCities(): ArrayList<String> {
        val cities = arrayListOf<String>()
        currentItems.forEach {
            cities.add(it.city)
        }
        return cities
    }

    fun getFilterBundle(): Bundle {
        val bundle = Bundle()
        val cities = getCurrentCities()
        bundle.putStringArrayList("cities", cities)
        bundle.putString("chosenCity", cityFilter)

        val categories = arrayListOf<String>()
        categoriesFilter.forEach {
            categories.add(it.name) // uppercase values
        }
        bundle.putStringArrayList("chosenCategories", categories)
        return bundle
    }

    fun detachListeners() {
        itemRepository.detachHomeListeners()
    }
}
