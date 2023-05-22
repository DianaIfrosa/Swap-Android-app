package com.diana.bachelorthesis.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.repository.PhotoRepository
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback
import com.google.firebase.auth.EmailAuthProvider

class ProfileViewModel : ViewModel() {
    private val TAG: String = ProfileViewModel::class.java.name

    private val itemRepository = ItemRepository.getInstance()
    private val photosRepository = PhotoRepository.getInstance()
    private val userRepository = UserRepository.getInstance()

    var profilePhoto: String? = null
    var newprofilePhoto: Uri? = null

    var notificationsOptionSelected: Int =
        2 // default = option regarding the preferred items

    var preferredOwners: MutableList<String> = mutableListOf()
    var preferredCities: MutableList<String> = mutableListOf()
    var preferredWords: MutableList<String> = mutableListOf()
    var preferredCategories: MutableList<ItemCategory> = mutableListOf()
    var preferredExchangePreferences: MutableList<ItemCategory> = mutableListOf()

    fun getAllCities(callback: ListParamCallback<String>) {
        var retrievedCitiesExchanges = false
        var retrievedCitiesDonations = false

        val cities = mutableSetOf<String>()
        itemRepository.getExchangeItemsCities(object : ListParamCallback<String> {
            override fun onComplete(values: ArrayList<String>) {
                values.forEach {
                    cities.add(it)
                }
                retrievedCitiesExchanges = true
                if (retrievedCitiesDonations) {
                    callback.onComplete(ArrayList(cities))
                }
            }

            override fun onError(e: Exception?) {
                retrievedCitiesExchanges = true
                if (retrievedCitiesDonations) {
                    callback.onComplete(ArrayList(cities))
                }
            }

        })

        itemRepository.getDonationsItemsCities(object : ListParamCallback<String> {
            override fun onComplete(values: ArrayList<String>) {
                values.forEach {
                    cities.add(it)
                }
                retrievedCitiesDonations = true
                if (retrievedCitiesExchanges) {
                    callback.onComplete(ArrayList(cities))
                }
            }

            override fun onError(e: Exception?) {
                retrievedCitiesDonations = true
                if (retrievedCitiesExchanges) {
                    callback.onComplete(ArrayList(cities))
                }
            }
        })
    }

    fun saveNewProfilePhoto(userEmail: String, photoUri: Uri, callback: OneParamCallback<String>) {
        photosRepository.uploadProfilePhoto(userEmail, photoUri, callback, false)
    }

    fun verifyOldPass(oldPassword: String, callback: NoParamCallback){
        val credential = EmailAuthProvider.getCredential(userRepository.auth.currentUser!!.email!!, oldPassword)
        userRepository.auth.currentUser!!.reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "Old password was correct.")
                callback.onComplete()
            } else {
                Log.d(TAG, "An error occurred during re-authentication.")
                if (it.exception != null) {
                    Log.d(TAG, it.exception.toString())
                }
                callback.onError(it.exception)
            }
        }
    }

}