package com.diana.bachelorthesis.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.model.ItemCondition
import com.diana.bachelorthesis.model.ItemDonation
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.repository.ItemRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.util.*

class AddItemViewModel : ViewModel() {
    private val TAG: String = AddItemViewModel::class.java.name
    private val repository = ItemRepository.getInstance()

    var categoryChosen: ItemCategory = ItemCategory.UNKNOWN
    var conditionChosen: ItemCondition? = null

    var itemForExchange: Boolean = true
    var itemTitle: String = ""
    var itemDescription: String = ""
    var itemYear: Int? = null
    var itemPhotos: ArrayList<Uri> = arrayListOf()
    var itemExchangePreferences:  ArrayList<ItemCategory> = arrayListOf()
    var itemLocation: GeoPoint? = null

    fun addItem(){
        val item = if (itemForExchange) ItemExchange()
        else ItemDonation()

        item.itemId =  UUID.randomUUID().toString()
        item.name = itemTitle
        item.description = itemDescription
        item.category = categoryChosen
        item.condition = conditionChosen
        item.postDate = Timestamp.now()
        item.year = itemYear
        // TODO add photos to cloud then retrieve urls and assign to item
        // TODO get city from geopoint and assign to item

        item.location = itemLocation!!
        // TODO get user's identity and assign to item

        if (itemForExchange)
            (item as ItemExchange).exchangePreferences = itemExchangePreferences
    }

    fun saveItemData(forExchange: Boolean, title: String, description: String, year: Int?, exchangePreferences: ArrayList<ItemCategory>) {
        itemForExchange = forExchange
        itemTitle = title
        itemDescription = description
        itemYear = year
        itemExchangePreferences= exchangePreferences
    }
}