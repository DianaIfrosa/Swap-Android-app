package com.diana.bachelorthesis.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diana.bachelorthesis.OnCompleteCallback
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemDonation
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.viewmodel.HomeViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.w3c.dom.Document
import javax.inject.Singleton

@Singleton
class ItemRepository {
    private val TAG: String = ItemRepository::class.java.getName()
    val db = Firebase.firestore
    // val homeViewModel = HomeViewModel.getInstance()

    // for cloud
    val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageReference: StorageReference = firebaseStorage.reference

    val EXCHANGE_COLLECTION = "ExchangeItems"
    val DONATION_COLLECTION = "DonationItems"

    companion object {
        @Volatile
        private var instance: ItemRepository? = null
        fun getInstance() = instance ?: ItemRepository().also { instance = it }
    }

    // TODO  de analizat folosirea cache-lui : https://firebase.google.com/docs/firestore/query-data/get-data

    fun getItems(forExchange: Boolean, callback: OnCompleteCallback) {
        val collRef: CollectionReference = if (forExchange) {
            db.collection(EXCHANGE_COLLECTION)
        } else {
            db.collection(DONATION_COLLECTION)
        }

        collRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val allItems = ArrayList<Item>()
                val documents = snapshot.documents
                documents.forEach {
                    val item =  if (forExchange) it.toObject(ItemExchange::class.java) else it.toObject(ItemDonation::class.java)
                    if (item != null) {
                        Log.d(TAG, "Retrieved item ${it.data}")
                        allItems.add(item)
                    }
                }
                callback.onCompleteGetItems(allItems)
            } else {
                Log.w(TAG, "No such snapshot $snapshot")
            }
        }
        // TODO sort by post date
    }

// TODO write the rest of the CRUD operations and use picasso for images;
// also store images in cloud firebase
}