package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.view.MapFragment
import com.google.android.gms.maps.model.Marker

class MapViewModel : ViewModel() {
    private val TAG: String = MapViewModel::class.java.name

    var donationItems: List<Item> = arrayListOf()
    var exchangeITems: List<Item> = arrayListOf()
    var allItems: MutableList<Item> = arrayListOf()
    val itemMarkerMap: HashMap<Marker, Item> = hashMapOf()

    fun updateItems(forExchange: Boolean, items: List<Item>) {
        if (forExchange) {
            exchangeITems = items
        } else {
            donationItems = items
        }

        allItems = (exchangeITems + donationItems) as MutableList<Item>
        // TODO sort by location selected/ user's location ?

    }
    fun addItemMarkerPair(item: Item, marker: Marker?) {
        if (marker != null)
            itemMarkerMap[marker] = item
    }

    fun getItemFromMarker(marker: Marker): Item? = itemMarkerMap[marker]
}