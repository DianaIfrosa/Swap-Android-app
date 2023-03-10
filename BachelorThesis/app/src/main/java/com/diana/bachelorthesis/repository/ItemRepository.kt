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

    val COLLECTION_NAME = "Objects"
    val EXCHANGE_DOCUMENT = "Exchange"
    val DONATION_DOCUMENT = "Donation"

    companion object {
        @Volatile
        private var instance: ItemRepository? = null
        fun getInstance() = instance ?: ItemRepository().also { instance = it }
    }

    // TODO  de analizat folosirea cache-lui : https://firebase.google.com/docs/firestore/query-data/get-data



    fun getItem(id: String, owner: String, isExchange: Boolean): Item? {
        val docRef: DocumentReference
        var item: Item? = null
        if (isExchange) {
            docRef = db.collection(COLLECTION_NAME).document(EXCHANGE_DOCUMENT)
        } else {
            docRef = db.collection(COLLECTION_NAME).document(DONATION_DOCUMENT)
        }

        docRef.collection(owner).document(id).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    item = if (isExchange) document.toObject<ItemExchange>()
                    else document.toObject<ItemDonation>()
                } else {
                    Log.d(TAG, "No such item with id $id, owner $owner and isExchange $isExchange")
                }
            } else {
                Log.w(
                    TAG,
                    "Error getting item with id $id, owner $owner and isExchange $isExchange",
                    task.exception
                )
            }
        }
        return item
    }

    fun getOwners(forExchange: Boolean, callback: OnCompleteCallback){
        var owners: ArrayList<String>
        val docRef = if (forExchange) db.collection(COLLECTION_NAME).document(EXCHANGE_DOCUMENT)
        else db.collection(COLLECTION_NAME).document(DONATION_DOCUMENT)

        docRef.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null) {
                        Log.d(TAG, "Owners retrieved for exchange $forExchange")
                        owners = document.get("owners") as ArrayList<String>
                        callback.onCompleteGetOwners(owners)
                    } else {
                        Log.d(TAG, "No owners field found")
                    }

                } else {
                    Log.w(TAG, "Error getting owners ", task.exception)

                }
            }
    }


// TODO write the rest of the CRUD operations and use picasso for images;
// also store images in cloud firebase
}