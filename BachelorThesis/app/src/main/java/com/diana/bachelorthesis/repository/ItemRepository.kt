package com.diana.bachelorthesis.repository

import android.util.Log
import com.diana.bachelorthesis.model.History
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemDonation
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Singleton

@Singleton
class ItemRepository {
    private val TAG: String = ItemRepository::class.java.name
    val db = Firebase.firestore

    val EXCHANGE_COLLECTION = "ExchangeItems"
    val DONATION_COLLECTION = "DonationItems"

    var itemsExchangeListenerRegistration: ListenerRegistration? = null
    var itemsDonationsListenerRegistration: ListenerRegistration? = null

    var favDonationsListenerRegistration: ListenerRegistration? = null
    var favExchangesListenerRegistration: ListenerRegistration? = null

    companion object {
        @Volatile
        private var instance: ItemRepository? = null
        fun getInstance() = instance ?: ItemRepository().also { instance = it }
    }

    // TODO  de analizat folosirea cache-lui : https://firebase.google.com/docs/firestore/query-data/get-data

    fun getExchangeItem(itemId: String, callback: OneParamCallback<Item>) {
        db.collection(EXCHANGE_COLLECTION).document(itemId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val item = task.result.toObject(ItemExchange::class.java)
                callback.onComplete(item)
            } else {
                callback.onError(task.exception)
            }
        }
    }

    fun getDonationItem(itemId: String, callback: OneParamCallback<Item>) {
        db.collection(DONATION_COLLECTION).document(itemId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val item = task.result.toObject(ItemDonation::class.java)
                callback.onComplete(item)
            } else {
                callback.onError(task.exception)
            }
        }
    }

    fun getExchangeItemsAndListen(callback: ListParamCallback<Item>) {
        itemsExchangeListenerRegistration =
            db.collection(EXCHANGE_COLLECTION).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for exchange items", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val allItems = ArrayList<Item>()
                    val documents = snapshot.documents
                    documents.forEach {
                        val item = it.toObject(ItemExchange::class.java)
                        if (item != null) {
                            Log.d(TAG, "Retrieved exchange item ${it.data}")
                            allItems.add(item)
                        }
                    }
                    callback.onComplete(allItems)
                } else {
                    Log.w(TAG, "No such snapshot")
                }
            }
    }

    fun getDonationItemsAndListen(callback: ListParamCallback<Item>) {
        itemsDonationsListenerRegistration =
            db.collection(DONATION_COLLECTION).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for donation items", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val allItems = ArrayList<Item>()
                    val documents = snapshot.documents
                    documents.forEach {
                        val item = it.toObject(ItemDonation::class.java)
                        if (item != null) {
                            allItems.add(item)
                        }
                    }
                    callback.onComplete(allItems)
                } else {
                    Log.w(TAG, "No such snapshot")
                }
            }
    }

    fun getFavoriteDonations(
        favoriteItemsIds: List<String>,
        callback: ListParamCallback<Pair<Int, Item>>
    ) {
        favDonationsListenerRegistration =
            db.collection(DONATION_COLLECTION).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for favorite donations", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {

                    val favItems = ArrayList<Pair<Int, Item>>() // save the item and its position in
                    // order to recreate the order the used added the item to its favorites

                    val documents = snapshot.documents
                    documents.forEach {
                        val item = it.toObject(ItemDonation::class.java)
                        if (item != null) {
                            val position = favoriteItemsIds.indexOfFirst { id -> item.itemId == id }
                            if (position != -1) {
                                Log.d(TAG, "Retrieved favorite donation item ${it.data}")
                                favItems.add(Pair(position, item))
                            }
                        }
                    }
                    callback.onComplete(favItems)
                } else {
                    Log.w(TAG, "No such snapshot")
                }
            }
    }

    fun getFavoriteExchanges(
        favoriteItemsIds: List<String>,
        callback: ListParamCallback<Pair<Int, Item>>
    ) {
        favExchangesListenerRegistration =
            db.collection(EXCHANGE_COLLECTION).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for favorite exchanges", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {

                    val favItems = ArrayList<Pair<Int, Item>>() // save the item and its position in
                    // order to recreate the order the used added the item to its favorites

                    val documents = snapshot.documents
                    documents.forEach {
                        val item = it.toObject(ItemExchange::class.java)
                        if (item != null) {
                            val position = favoriteItemsIds.indexOfFirst { id -> item.itemId == id }
                            if (position != -1) {
                                Log.d(TAG, "Retrieved favorite  exchange item ${it.data}")
                                favItems.add(Pair(position, item))
                            }
                        }
                    }
                    callback.onComplete(favItems)
                } else {
                    Log.w(TAG, "No such snapshot")
                    callback.onError(null)
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
    }

    fun addItem(item: Item, callback: NoParamCallback) {
        val reference = if (item is ItemExchange) db.collection(EXCHANGE_COLLECTION)
        else db.collection(DONATION_COLLECTION)

        reference.document(item.itemId).set(item)
            .addOnSuccessListener {
                Log.d(TAG, "Item successfully added!")
                callback.onComplete()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
                callback.onError(e)
            }
    }

    fun getExchangeItemsCities(callback: ListParamCallback<String>) {
        db.collection(EXCHANGE_COLLECTION).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Retrieved all exchange items.")
                val allCities = ArrayList<String>()
                task.result.forEach {
                    val item = it.toObject(ItemExchange::class.java)
                    allCities.add(item.city)
                }
                callback.onComplete(allCities)
            } else {
                Log.w(
                    TAG,
                    "Error while retrieving all exchange items, see message below"
                )
                if (task.exception != null) {
                    Log.w(TAG, task.exception!!.message.toString())
                }
                callback.onError(task.exception)
            }
        }
    }

    fun getExchangeItems(callback: ListParamCallback<Item>) {
        db.collection(EXCHANGE_COLLECTION).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Retrieved all exchange items.")
                val allItems = ArrayList<Item>()
                task.result.forEach {
                    val item = it.toObject(ItemExchange::class.java)
                    allItems.add(item)
                }
                callback.onComplete(allItems)
            } else {
                Log.w(
                    TAG,
                    "Error while retrieving all exchange items, see message below"
                )
                if (task.exception != null) {
                    Log.w(TAG, task.exception!!.message.toString())
                }
                callback.onError(task.exception)
            }
        }
    }

    fun getDonationsItemsCities(callback: ListParamCallback<String>) {
        db.collection(DONATION_COLLECTION).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Retrieved all donation items.")
                val allCities = ArrayList<String>()
                task.result.forEach {
                    val item = it.toObject(ItemDonation::class.java)
                    allCities.add(item.city)
                }
                callback.onComplete(allCities)
            } else {
                Log.w(
                    TAG,
                    "Error while retrieving all exchange items, see message below"
                )
                if (task.exception != null) {
                    Log.w(TAG, task.exception!!.message.toString())
                }
                callback.onError(task.exception)
            }
        }
    }

    fun getDonationItems(callback: ListParamCallback<Item>) {
        db.collection(DONATION_COLLECTION).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Retrieved all donation items.")
                val allItems = ArrayList<Item>()
                task.result.forEach {
                    val item = it.toObject(ItemDonation::class.java)
                    allItems.add(item)
                }
                callback.onComplete(allItems)
            } else {
                Log.w(
                    TAG,
                    "Error while retrieving all donation items, see message below"
                )
                if (task.exception != null) {
                    Log.w(TAG, task.exception!!.message.toString())
                }
                callback.onError(task.exception)
            }
        }
    }

    fun deleteItem(itemId: String, isExchange: Boolean, callback: NoParamCallback) {
        val collRef = if (isExchange) db.collection(EXCHANGE_COLLECTION)
        else db.collection(DONATION_COLLECTION)

        collRef.document(itemId).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Deleted item with id $itemId.")
                callback.onComplete()
            } else {
                Log.w(
                    TAG,
                    "Error while deleting item with id $itemId, see message below"
                )
                if (task.exception != null) {
                    Log.w(TAG, task.exception!!.message.toString())
                }
                callback.onError(task.exception)
            }
        }
    }

    fun markExchangeItemAsGiven(historyId: String, itemId: String, callback: NoParamCallback) {
        db.collection(EXCHANGE_COLLECTION).document(itemId).update("exchangeInfo", historyId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.w(TAG, "Successfully marked item $itemId as given.")
                callback.onComplete()
            } else {
                Log.w(TAG, "Error occurred while marking item $itemId as given.")
                callback.onError(task.exception)
            }
        }
    }

    fun markDonationItemAsGiven(historyId: String, itemId: String, callback: NoParamCallback) {
        db.collection(DONATION_COLLECTION).document(itemId).update("donationInfo", historyId).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.w(TAG, "Successfully marked item $itemId as given.")
                callback.onComplete()
            } else {
                Log.w(TAG, "Error occurred while marking item $itemId as given.")
                callback.onError(task.exception)
            }
        }
    }

    fun getHistoryItems(currentPosition: Int, historyObjects: ArrayList<History>, result: ArrayList<Pair<Item, Item?>>, callback: ListParamCallback<Pair<Item, Item?>>) {
        val currentHistory = historyObjects[currentPosition]
        val forExchange = currentHistory.item2 != null

        var docRef: CollectionReference
        if (forExchange)
            docRef = db.collection(EXCHANGE_COLLECTION)
        else
            docRef = db.collection(DONATION_COLLECTION)

        docRef.document(currentHistory.item1).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val item1 = if (forExchange) task.result.toObject(ItemExchange::class.java) else task.result.toObject(ItemDonation::class.java)
                if (item1 != null) {
                    if (!forExchange) { // donation event
                        result.add(Pair(item1, null))
                        if (currentPosition == historyObjects.size - 1) {
                            callback.onComplete(result)
                        } else {
                            getHistoryItems(currentPosition + 1, historyObjects, result, callback)
                        }
                    } else { //exchange event

                        docRef.document(currentHistory.item2!!).get().addOnCompleteListener { task2 ->

                            if (task2.isSuccessful) {
                                val item2 = task.result.toObject(ItemExchange::class.java)
                                if (item2 != null) {
                                    result.add(Pair(item1, item2))
                                    if (currentPosition == historyObjects.size - 1) {
                                        callback.onComplete(result)
                                    } else {
                                        getHistoryItems(currentPosition + 1, historyObjects, result, callback)
                                    }
                                } else {
                                    Log.w(TAG, "Error while retrieving item with id ${currentHistory.item2!!}")
                                    callback.onError(task.exception)
                                }

                            } else {
                                Log.w(TAG, "Error while retrieving item with id ${currentHistory.item2!!}")
                                callback.onError(task.exception)
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "Error while retrieving item with id ${currentHistory.item1}")
                    callback.onError(task.exception)
                }

            } else {
                Log.w(TAG, "Error while retrieving item with id ${currentHistory.item1}")
                callback.onError(task.exception)
            }
        }
    }

    fun detachHomeListeners() {
        itemsDonationsListenerRegistration?.remove()
        itemsExchangeListenerRegistration?.remove()
    }
    fun detachFavDonationsListener() {
        favDonationsListenerRegistration?.remove()
    }

    fun detachFavExchangesListener() {
        favExchangesListenerRegistration?.remove()
    }

}