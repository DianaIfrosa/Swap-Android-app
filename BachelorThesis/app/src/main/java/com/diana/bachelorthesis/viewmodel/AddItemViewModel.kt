package com.diana.bachelorthesis.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.model.ItemCondition
import com.diana.bachelorthesis.model.ItemDonation
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.repository.PhotoRepository
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.LocationHelper
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class AddItemViewModel(var locationHelper: LocationHelper) : ViewModel() {
    private val TAG: String = AddItemViewModel::class.java.name
    private val itemRepository = ItemRepository.getInstance()
    private val photoRepository = PhotoRepository.getInstance()
    private val userRepository = UserRepository.getInstance()

    var categoryChosen: ItemCategory = ItemCategory.UNKNOWN
    var conditionChosen: ItemCondition? = null
    var photosUri: ArrayList<Uri> = arrayListOf()

    var itemForExchange: Boolean = true
    var itemTitle: String = ""
    var itemDescription: String = ""
    var itemYear: Int? = null
    var itemExchangePreferences:  ArrayList<ItemCategory> = arrayListOf()
    var itemLocation: GeoPoint? = null
    var itemOwner: String = userRepository.currentUser.email

    class ViewModelFactory(private val arg: LocationHelper) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddItemViewModel::class.java)) {
                return AddItemViewModel(arg) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    suspend fun addItem(){
        val item = if (itemForExchange) ItemExchange()
        else ItemDonation()

        item.itemId =  UUID.randomUUID().toString()
        item.name = itemTitle
        item.description = itemDescription
        item.category = categoryChosen
        item.condition = conditionChosen
        item.postDate = Timestamp.now()
        item.year = itemYear
        item.location = itemLocation!!
        item.owner = itemOwner

        if (itemForExchange)
            (item as ItemExchange).exchangePreferences = itemExchangePreferences

        coroutineScope {
            launch (context = Dispatchers.Default) {
                coroutineScope {
                    launch {
                        val photos = async {photoRepository.uploadPhotos(
                            itemOwner,
                            item.itemId,
                            photosUri,
                        )}
                        item.photos = photos.await() as ArrayList<String>
                    }

                    launch {
                        val itemCity = async { locationHelper.getItemCity(itemLocation!!) }
                        item.city = itemCity.await()
                    }
                }
                itemRepository.addItem(item)
            }
        }
    }

    fun restoreDefaultValues() {
        categoryChosen = ItemCategory.UNKNOWN
        conditionChosen = null
        photosUri = arrayListOf()

        itemForExchange = true
        itemTitle = ""
        itemDescription= ""
        itemYear = null
        itemExchangePreferences = arrayListOf()
        itemLocation = null
        itemOwner = userRepository.currentUser.email
    }


    fun saveItemData(forExchange: Boolean, title: String, description: String, year: Int?, exchangePreferences: ArrayList<ItemCategory>) {
        itemForExchange = forExchange
        itemTitle = title
        itemDescription = description
        itemYear = year
        itemExchangePreferences= exchangePreferences
    }
}