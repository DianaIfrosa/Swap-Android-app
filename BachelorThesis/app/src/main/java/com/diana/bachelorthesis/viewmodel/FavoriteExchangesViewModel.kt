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

class FavoriteExchangesViewModel: ViewModel() {
    private val TAG: String = FavoriteExchangesViewModel::class.java.name

    private val itemRepository = ItemRepository.getInstance()

    private val _exchangeItems = MutableLiveData<List<Item>>()
    var favoriteExchangesIds: List<String> = listOf()
    var exchangeItems: LiveData<List<Item>> = _exchangeItems

    fun populateLiveDataExchanges() {
        Log.d(TAG, "Populating live data exchanges in FavoritesViewModel.")
        itemRepository.getFavoriteExchanges(favoriteExchangesIds, object :
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
                if (valuesSortedAndFiltered != exchangeItems.value) {
                    // update the UI only when the selected items are updated
                    _exchangeItems.value = valuesSortedAndFiltered
                    Log.d(TAG, "Updated favorite exchange items")
                }
            }

            override fun onError(e: Exception?) {
                _exchangeItems.value = listOf()
            }
        })
    }
}