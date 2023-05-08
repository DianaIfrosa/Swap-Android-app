package com.diana.bachelorthesis.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.utils.ListParamCallback

class FavoritesViewModel : ViewModel() {
    private val TAG: String = FavoritesViewModel::class.java.name
    private val itemRepository = ItemRepository.getInstance()

    private val _exchangeItems = MutableLiveData<List<Item>>()
    private val _donationItems = MutableLiveData<List<Item>>()

    var favoriteDonationsIds: List<String> = listOf()
    var favoriteExchangesIds: List<String> = listOf()

    var exchangeItems: LiveData<List<Item>> = _exchangeItems
    val donationItems: LiveData<List<Item>> = _donationItems

    var lastScrollPosition = 0

    fun populateLiveDataExchanges() {
        Log.d(TAG, "Populating live data exchanges in FavoritesViewModel.")
        itemRepository.getFavoriteExchanges(favoriteExchangesIds, object : ListParamCallback<Pair<Int, Item>> {
            override fun onComplete(values: ArrayList<Pair<Int, Item>>) {
                val valuesSorted = ArrayList(values.sortedByDescending { item -> item.first }).map { it.second}
                if (valuesSorted != exchangeItems.value) {
                    // update the UI only when the selected items are updated
                    _exchangeItems.value = valuesSorted
                    Log.d(TAG, "Updated favorite exchange items")
                }
            }

            override fun onError(e: Exception?) {
                _exchangeItems.value = listOf()
            }
        })
    }

    fun populateLiveDataDonations() {
        Log.d(TAG, "Populating live data  donations in FavoritesViewModel.")
        itemRepository.getFavoriteDonations(favoriteDonationsIds, object : ListParamCallback<Pair<Int, Item>> {
            override fun onComplete(values: ArrayList<Pair<Int, Item>>) {
                val valuesSorted = ArrayList(values.sortedByDescending { item -> item.first }).map { it.second}
                if (valuesSorted != donationItems.value) {
                    // update the UI only when the selected items are updated
                    _donationItems.value = valuesSorted
                    Log.d(TAG, "Updated favorite donation items")
                }
            }

            override fun onError(e: Exception?) {
                _donationItems.value = listOf()
            }
        })
    }
    fun detachListeners() {
        itemRepository.detachFavoritesListener()
    }
}