package com.diana.bachelorthesis.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import com.diana.bachelorthesis.OnCompleteCallback
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.utils.LocationHelper
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList


class HomeViewModel(var locationHelper: LocationHelper) : ViewModel() {
    private val TAG: String = HomeViewModel::class.java.name
    private val repository = ItemRepository.getInstance()

    private val _exchangeItems = MutableLiveData<List<Item>>()
    private val _donationItems = MutableLiveData<List<Item>>()

    var exchangeItems: LiveData<List<Item>> = _exchangeItems
    val donationItems: LiveData<List<Item>> = _donationItems
    // TODO take into consideration to have current items as live data and only observe in fragment

    var currentItems: List<Item> = arrayListOf()
    var displayExchangeItems: Boolean = true

    private var searchText: String = ""
    var sortOption: Int = 0
    private var cityFilter: String = ""
    private var categoriesFilter: List<ItemCategory> = arrayListOf()

    init {
        populateLiveData()
    }

    class ViewModelFactory(private val arg: LocationHelper) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(arg) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private fun populateLiveData() {
        //_exchangeItems.value = repository.getItems(true)
        //_donationItems.value = repository.getItems(false)

        repository.getItems(true, object : OnCompleteCallback {
            override fun onCompleteGetItems(items: ArrayList<Item>) {
                _exchangeItems.value = items
                if (displayExchangeItems)
                    updateCurrentItems(items)

                viewModelScope.launch {
                    _exchangeItems.value = locationHelper.getItemsCities(items)
                    if (displayExchangeItems)
                        updateCurrentItems(items)
                }
            }
        })

        repository.getItems(false, object : OnCompleteCallback {
            override fun onCompleteGetItems(items: ArrayList<Item>) {
                _donationItems.value = items
                if (!displayExchangeItems)
                    updateCurrentItems(items)

                viewModelScope.launch {
                    _donationItems.value = locationHelper.getItemsCities(items)
                    if (!displayExchangeItems)
                        updateCurrentItems(items)
                }
            }
        })

    }

    fun updateCurrentItems(items: ArrayList<Item>) {
        // called when there is a change in DB and the data must be updated
        // and the user's search, sort, filter preferences should be restored
//        var itemsTemp: List<Item> = items

        currentItems = items
        if (searchText.isNotEmpty()) {
            searchItem(searchText)
        }

        applySort(sortOption)
        applyFilter(cityFilter, categoriesFilter)
    }

    private fun restoreDefaultOptions() {
        sortOption = 0
        searchText = ""
        cityFilter = ""
        categoriesFilter = arrayListOf()
    }

    fun searchItem(inputText: String) {
        // modifies the currentItems value accordingly

        val result: MutableList<Item> = arrayListOf()

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

    fun restoreDefaultCurrentItems() {
        // restore default sort option and remove filter options selected
        restoreDefaultOptions()

        currentItems = if (displayExchangeItems) _exchangeItems.value as ArrayList
        else _donationItems.value as ArrayList
    }

    fun applySort(option: Int) {
        Log.d(TAG, "Sort by option $option")
        sortOption = option
        currentItems = sort(option, currentItems)
    }

    private fun sort(option: Int, items: List<Item>): List<Item> {
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
        return itemsTemp
    }

    fun applyFilter(city: String, categories: List<ItemCategory>) {
        Log.d(
            TAG,
            "Filter by city $city and categories ${categories.joinToString { it.displayName }}"
        )
        cityFilter = city
        categoriesFilter = categories

        val rawData = if (displayExchangeItems) _exchangeItems.value as ArrayList
        else _donationItems.value as ArrayList

        // apply sort on raw data then filter, to manage check and uncheck situations
        currentItems = filter(city, categories, sort(sortOption, rawData))
    }

    private fun filter(
        city: String,
        categories: List<ItemCategory>,
        items: List<Item>
    ): List<Item> {
        var itemsTemp = items

        if (city.isNotEmpty()) {
            itemsTemp = itemsTemp.filter { item -> item.city == city }
        }

        if (categories.isNotEmpty()) {
            itemsTemp = itemsTemp.filter { item -> item.category in categories }
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
}
