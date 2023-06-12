package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Item
import com.google.android.gms.maps.model.Marker

class MapViewModel : ViewModel() {
    private val TAG: String = MapViewModel::class.java.name

    private var donationItems: List<Item> = arrayListOf()
    private var exchangeITems: List<Item> = arrayListOf()
    var allItems: MutableList<Item> = arrayListOf()
    private val itemMarkerMap: HashMap<Marker, Item> = hashMapOf()

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