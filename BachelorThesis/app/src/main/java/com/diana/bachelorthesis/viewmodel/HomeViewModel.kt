package com.diana.bachelorthesis.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.OnCompleteCallback
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemDonation
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.repository.ItemRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


class HomeViewModel : ViewModel() {
    private val TAG: String = HomeViewModel::class.java.getName()
    private val _text = MutableLiveData<String>().apply {
        value = "15 produse"
    }

    val db = Firebase.firestore

    val COLLECTION_NAME = "Objects"
    val EXCHANGE_DOCUMENT = "Exchange"
    val DONATION_DOCUMENT = "Donation"

    val text: LiveData<String> = _text

    private val _exchangeItems = MutableLiveData<ArrayList<Item>>()
    private val _donationItems = MutableLiveData<ArrayList<Item>>()

    var exchangeItems: LiveData<ArrayList<Item>> = _exchangeItems
    val donationItems: LiveData<ArrayList<Item>> = _donationItems

    val repository = ItemRepository.getInstance()

    init {
        Log.d(TAG, "I am in init")
        populateLiveData()
    }

    private fun populateLiveData() {
        //_exchangeItems.value = repository.getItems(true)
        //_donationItems.value = repository.getItems(false)

        getItems(true)
        getItems(false)
    }

    fun getItems(isExchange: Boolean) {
        repository.getOwners(isExchange, object : OnCompleteCallback {
            override fun onCompleteGetOwners(owners: ArrayList<String>) {
               getItemsFromOwners(isExchange, owners)
            }
        }

        )
    }

    fun getItemsFromOwners(isExchange: Boolean, owners: ArrayList<String>) {
        val docRef: DocumentReference = if (isExchange) {
            db.collection(COLLECTION_NAME).document(EXCHANGE_DOCUMENT)
        } else {
            db.collection(COLLECTION_NAME).document(DONATION_DOCUMENT)
        }
        val allItems = ArrayList<Item>()

        // take objects for each owner
        docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                allItems.clear()
                for (owner in owners) {
                    docRef.collection(owner).get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val documents = task.result
                            if (documents != null) {
                                Log.d(
                                    TAG,
                                    "Retrieving ${documents.size()} items for owner $owner"
                                )
                                for (document in documents) {
                                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                                    val item =
                                        if (isExchange) document.toObject<ItemExchange>()
                                        else document.toObject<ItemDonation>()
                                    allItems.add(item)
                                }
                                if (isExchange) {
                                    _exchangeItems.value = allItems
                                } else {
                                    _donationItems.value = allItems
                                }
                            } else {
                                Log.d(
                                    TAG,
                                    "No documents found for owner $owner"
                                )
                            }
                        } else {
                            Log.w(
                                TAG,
                                "Error getting items for owner $owner and isExchange $isExchange",
                                task.exception
                            )
                        }
                    }
                }
            }
        }
        // TODO sort by post date
    }
}