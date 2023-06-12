package com.diana.bachelorthesis.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CategoriesRepository {
    private val TAG: String = CategoriesRepository::class.java.name
    val db = Firebase.firestore

    private val COLLECTION_NAME = "CategoriesObjects"

    companion object {
        @Volatile
        private var instance: CategoriesRepository? = null
        fun getInstance() = instance ?: CategoriesRepository().also { instance = it }
    }

    fun addAvailableItemToCategory(category: String) {
        db.collection(COLLECTION_NAME).document(category).update(
            "availableItems", FieldValue.increment(1),
            "totalItems", FieldValue.increment(1)
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Successfully increased items count for category $category")
            } else {
                Log.d(TAG, "Could not increase items count for category $category")
            }
        }
    }

    fun removeItemFromCategory(category: String) {
        db.collection(COLLECTION_NAME).document(category).update(
            "availableItems", FieldValue.increment(-1),
            "totalItems", FieldValue.increment(-1)
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Successfully decreased items count for category $category")
            } else {
                Log.d(TAG, "Could not decrease items count for $category")
            }
        }
    }

    fun markItemAsGiven(category: String) {
        db.collection(COLLECTION_NAME).document(category).update(
            "availableItems", FieldValue.increment(-1)
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Successfully decreased available items count for category $category")
            } else {
                Log.d(TAG, "Could not decrease available items count for $category")
            }
        }
    }
}