package com.diana.bachelorthesis.repository

import android.util.Log
import com.diana.bachelorthesis.model.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UserRepository {

    private val TAG: String = UserRepository::class.java.getName()
    val db = Firebase.firestore

    // for cloud
    val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageReference: StorageReference = firebaseStorage.reference

    val COLLECTION_NAME = "Users"

    companion object {
        @Volatile
        private var instance: ItemRepository? = null
        fun getInstance() =
            instance ?: ItemRepository().also { instance = it }
    }

    fun addUser(userToAdd: User) {
        db.collection(COLLECTION_NAME)
            .add(userToAdd)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Successfull addition of DocumentSnapshot with id ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document")
            }
    }
}