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
}