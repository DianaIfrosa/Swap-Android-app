package com.diana.bachelorthesis.repository

import android.util.Log
import com.diana.bachelorthesis.OnCompleteCallback
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemDonation
import com.diana.bachelorthesis.model.ItemExchange
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import javax.inject.Singleton

@Singleton
class ItemRepository {
    private val TAG: String = ItemRepository::class.java.name
    val db = Firebase.firestore

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

//        Log.d(TAG, "inainte de listener" + Thread.currentThread().toString())

        collRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
//                Log.d(TAG, "in listener" + Thread.currentThread().toString())

                val allItems = ArrayList<Item>()
                val documents = snapshot.documents
                documents.forEach {
                    val item =  if (forExchange) it.toObject(ItemExchange::class.java)
                    else it.toObject(ItemDonation::class.java)
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
    }

    fun getItemsFromOwner(owner: String, forExchange: Boolean, callback: OnCompleteCallback) {
        val collRef: CollectionReference = if (forExchange) {
            db.collection(EXCHANGE_COLLECTION)
        } else {
            db.collection(DONATION_COLLECTION)
        }

        collRef.whereEqualTo("owner", owner)
            .get()
            .addOnSuccessListener { documents ->
                val allItems = ArrayList<Item>()
                documents.forEach {
                    val item =  if (forExchange) it.toObject(ItemExchange::class.java) else it.toObject(ItemDonation::class.java)
                    Log.d(TAG, "Retrieved item ${it.data}")
                    allItems.add(item)
                }
                callback.onCompleteGetItems(allItems)
            }
        // TODO some sorting maybe?
    }

// TODO write the rest of the CRUD operations and use picasso for images;
// also store images in cloud firebase
}