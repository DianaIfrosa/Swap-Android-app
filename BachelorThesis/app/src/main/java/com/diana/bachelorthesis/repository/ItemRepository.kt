package com.diana.bachelorthesis.repository

import android.util.Log
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemDonation
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.utils.OneParamCallback
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Singleton

@Singleton
class ItemRepository {
    private val TAG: String = ItemRepository::class.java.name
    val db = Firebase.firestore

    val EXCHANGE_COLLECTION = "ExchangeItems"
    val DONATION_COLLECTION = "DonationItems"

    var itemsListenerRegistration: ListenerRegistration? = null

    companion object {
        @Volatile
        private var instance: ItemRepository? = null
        fun getInstance() = instance ?: ItemRepository().also { instance = it }
    }

    // TODO  de analizat folosirea cache-lui : https://firebase.google.com/docs/firestore/query-data/get-data

    fun getItems(forExchange: Boolean, callback: ListParamCallback<Item>) {
        val collRef: CollectionReference = if (forExchange) {
            db.collection(EXCHANGE_COLLECTION)
        } else {
            db.collection(DONATION_COLLECTION)
        }

        itemsListenerRegistration = collRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {

                val allItems = ArrayList<Item>()
                val documents = snapshot.documents
                documents.forEach {
                    val item = if (forExchange) it.toObject(ItemExchange::class.java)
                    else it.toObject(ItemDonation::class.java)
                    if (item != null) {
                        Log.d(TAG, "Retrieved item ${it.data}")
                        allItems.add(item)
                    }
                }
                callback.onComplete(allItems)
            } else {
                Log.w(TAG, "No such snapshot $snapshot")
            }
        }
    }

    fun getItemsFromOwner(owner: String, forExchange: Boolean, callback: ListParamCallback<Item>) {
        val collRef: CollectionReference = if (forExchange) {
            db.collection(EXCHANGE_COLLECTION)
        } else {
            db.collection(DONATION_COLLECTION)
        }

        collRef.whereEqualTo("owner", owner).get().addOnSuccessListener { documents ->
                val allItems = ArrayList<Item>()
                documents.forEach {
                    val item =
                        if (forExchange) it.toObject(ItemExchange::class.java) else it.toObject(
                            ItemDonation::class.java
                        )
                    Log.d(TAG, "Retrieved item ${it.data}")
                    allItems.add(item)
                }
                callback.onComplete(allItems)
            }
        // TODO some sorting maybe?
    }

    suspend fun addItem(item: Item) {
        coroutineScope {
            val reference = if (item is ItemExchange) db.collection(EXCHANGE_COLLECTION)
            else db.collection(DONATION_COLLECTION)

            async(Dispatchers.Default) {
                reference.document(item.itemId).set(item)
                    .addOnSuccessListener { Log.d(TAG, "Item successfully added!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
            }.await()
        }
    }

    fun checkIsFavorite(itemId: String, oneParamCallback: OneParamCallback<Boolean>) {

    }

    fun detachListeners() {
        itemsListenerRegistration?.remove()
    }


// TODO write the rest of the CRUD operations and use picasso for images;
// also store images in cloud firebase
}