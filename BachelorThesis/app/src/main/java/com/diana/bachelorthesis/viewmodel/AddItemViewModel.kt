package com.diana.bachelorthesis.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.*
import com.diana.bachelorthesis.repository.CategoriesRepository
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.repository.PhotoRepository
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.util.*
import kotlin.collections.ArrayList

class AddItemViewModel : ViewModel() {
    private val TAG: String = AddItemViewModel::class.java.name
    private val itemRepository = ItemRepository.getInstance()
    private val photoRepository = PhotoRepository.getInstance()
    private val categoriesRepository = CategoriesRepository.getInstance()

    var categoryChosen: ItemCategory = ItemCategory.UNKNOWN
    var conditionChosen: ItemCondition? = null
    var photosUri: ArrayList<Uri> = arrayListOf()

    var itemForExchange: Boolean = true
    var itemTitle: String = ""
    var itemAddress: String = ""
    var itemCity: String = ""
    var itemDescription: String = ""
    var itemYear: Int? = null
    var itemExchangePreferences:  ArrayList<ItemCategory> = arrayListOf()
    var itemLocation: GeoPoint? = null
    var itemOwner: String = ""

     fun addItem(callback: OneParamCallback<Item>){
        val item = if (itemForExchange) ItemExchange()
        else ItemDonation()

        item.itemId =  UUID.randomUUID().toString()
        item.name = itemTitle
        item.description = itemDescription
        item.city = itemCity
        item.address = itemAddress
        item.category = categoryChosen
        item.condition = conditionChosen
        item.postDate = Timestamp.now()
        item.year = itemYear
        item.location = itemLocation!!
        item.owner = itemOwner

        if (itemForExchange)
            (item as ItemExchange).exchangePreferences = itemExchangePreferences

        photoRepository.uploadItemPhotos(
                            itemOwner,
                            item.itemId,
                            photosUri,
            0,
            arrayListOf(),
            object: ListParamCallback<String> {
                override fun onComplete(values: ArrayList<String>) {
                    item.photos = values
                    itemRepository.addItem(item, object: NoParamCallback {
                        override fun onComplete() {
                            categoriesRepository.addAvailableItemToCategory(item.category.name)
                            callback.onComplete(item)
                        }

                        override fun onError(e: Exception?) {
                          callback.onError(e)
                        }

                    })
                }

                override fun onError(e: Exception?) {
                   callback.onError(e)
                }
            })
    }

    fun restoreDefaultValues() {
        categoryChosen = ItemCategory.UNKNOWN
        conditionChosen = null
        photosUri = arrayListOf()

        itemForExchange = true
        itemTitle = ""
        itemAddress = ""
        itemCity = ""
        itemDescription= ""
        itemYear = null
        itemExchangePreferences = arrayListOf()
        itemLocation = null
        itemOwner = ""
    }


    fun saveItemData(forExchange: Boolean, title: String, description: String, year: Int?, exchangePreferences: ArrayList<ItemCategory>, ownerEmail: String) {
        itemForExchange = forExchange
        itemTitle = title
        itemDescription = description
        itemYear = year
        itemExchangePreferences= exchangePreferences
        itemOwner = ownerEmail
    }
}