package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.view.MapFragment

class MapViewModel : ViewModel() {
    private val TAG: String = MapViewModel::class.java.name

    var donationItems: List<Item> = arrayListOf()
    var exchangeITems: List<Item> = arrayListOf()
    var allItems: MutableList<Item> = arrayListOf()

    fun updateItems(forExchange: Boolean, items: List<Item>) {
        if (forExchange) {
            exchangeITems = items
        } else {
            donationItems = items
        }

        allItems = (exchangeITems + donationItems) as MutableList<Item>
        // TODO sort by location selected/ user's location ?

    }


}