package com.diana.bachelorthesis.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.model.ItemDonation
import com.diana.bachelorthesis.model.User

class MainViewModel : ViewModel() {
    private val TAG: String = MainViewModel::class.java.name
    var currentUser: User? = null

    var clickedOnRecommendations = false
    var modifiedRecommendations = false

    fun addFavoriteItem(item: Item) {
        Log.d(TAG, "Added item ${item.name} to favorites.")
        if (item is ItemDonation) {
            currentUser!!.favoriteDonations.add(item.itemId)
        } else {
            currentUser!!.favoriteExchanges.add(item.itemId)
        }
    }

    fun updateNotificationOption(option: Int) {
        currentUser!!.notifications.notificationsOption = option
    }

    fun itemIsFavorite(item: Item): Boolean {
        return if (item is ItemDonation) {
            (currentUser!!.favoriteDonations.find {
                it == item.itemId
            } != null)
        } else {
            (currentUser!!.favoriteExchanges.find {
                it == item.itemId
            } != null)
        }
    }

    fun removeFavoriteItem(item: Item) {
        Log.d(TAG, "Removed item ${item.name} from favorites.")
        if (item is ItemDonation) {
            currentUser!!.favoriteDonations.removeAll { it == item.itemId }
        } else {
            currentUser!!.favoriteExchanges.removeAll { it == item.itemId }
        }
    }

    fun updateUserPreferences(words: List<String>,
                              owners: List<String>,
                              cities: List<String>,
                              categories: List<ItemCategory>,
                              exchangePreferences: List<ItemCategory>) {
        currentUser!!.notifications.preferredWords = words
        currentUser!!.notifications.preferredOwners = owners
        currentUser!!.notifications.preferredCities = cities
        currentUser!!.notifications.preferredCategories = categories
        currentUser!!.notifications.preferredExchangePreferences = exchangePreferences
    }

    fun updateProfilePhoto(photo: String) {
        currentUser!!.profilePhoto = photo
    }
}