package com.diana.bachelorthesis.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemDonation
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.UserRepository

class MainViewModel : ViewModel() {
    // The purpose of this view model is to keep a reference to the user repository that saves the
    // current user, so that the User object will not be deleted by the garbage collector until the
    // app is completely closed.

    private val TAG: String = MainViewModel::class.java.name
    var currentUser: User? = null

    fun addFavoriteItem(item: Item) {
        Log.d(TAG, "Added item ${item.name} to favorites.")
        if (item is ItemDonation) {
            currentUser!!.favoriteDonations.add(item.itemId)
        } else {
            currentUser!!.favoriteExchanges.add(item.itemId)
        }
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
}