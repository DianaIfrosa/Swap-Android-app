package com.diana.bachelorthesis.repository

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Singleton

@Singleton
class UserTokensRepository {

    private val TAG: String = UserTokensRepository::class.java.name
    val db = Firebase.firestore

    val COLLECTION_NAME = "UserTokens"

    companion object {
        @Volatile
        private var instance: UserTokensRepository? = null
        fun getInstance() = instance ?: UserTokensRepository().also { instance = it }
    }

    fun addUserToken(email: String, token: String) {
        val data = mutableMapOf<String, String>()
        data["token"] = token

        db.collection(COLLECTION_NAME).document(email).set(data).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Successfully set token to user $email")
            } else {
                Log.w(TAG, "Could not set token to user $email")
            }
        }
    }

    fun deleteToken(email :String) {
        db.collection(COLLECTION_NAME).document(email).delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Successfully deleted token for user $email")
            } else {
                Log.w(TAG, "Could not delete token for user $email")
            }
        }
    }
}