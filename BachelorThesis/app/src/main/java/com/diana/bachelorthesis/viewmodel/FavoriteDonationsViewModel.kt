package com.diana.bachelorthesis.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemDonation
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.utils.ListParamCallback

class FavoriteDonationsViewModel: ViewModel() {
    private val TAG: String = FavoriteDonationsViewModel::class.java.name

    private val itemRepository = ItemRepository.getInstance()

    private val _donationItems = MutableLiveData<List<Item>>()
    var favoriteDonationsIds: List<String> = listOf()
    val donationItems: LiveData<List<Item>> = _donationItems

    fun populateLiveDataDonations() {
        Log.d(TAG, "Populating live data  donations in FavoritesViewModel.")
        itemRepository.getFavoriteDonations(favoriteDonationsIds, object :
            ListParamCallback<Pair<Int, Item>> {
            override fun onComplete(values: ArrayList<Pair<Int, Item>>) {
                val valuesSorted = ArrayList(values.sortedByDescending { item -> item.first }).map { it.second}
                val valuesSortedAndFiltered = ArrayList(valuesSorted.filter { item ->
                    when (item) {
                        is ItemExchange -> {
                            (item.exchangeInfo == null)
                        }
                        is ItemDonation -> {
                            (item.donationInfo == null)
                        }
                        else -> false
                    }
                })
                if (valuesSortedAndFiltered != donationItems.value) {
                    // update the UI only when the selected items are updated
                    _donationItems.value = valuesSortedAndFiltered
                    Log.d(TAG, "Updated favorite donation items")
                }
            }

            override fun onError(e: Exception?) {
                _donationItems.value = listOf()
            }
        })
    }

    fun detachListener() {
        itemRepository.detachFavDonationsListener()
    }
}