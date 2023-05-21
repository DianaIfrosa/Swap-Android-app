package com.diana.bachelorthesis.repository

import android.util.Log
import com.diana.bachelorthesis.model.Mail
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Singleton

@Singleton
class MailRepository {

    private val TAG: String = MailRepository::class.java.name
    val db = Firebase.firestore
    val COLLECTION_NAME = "mail"

    companion object {
        @Volatile
        private var instance: MailRepository? = null
        fun getInstance() = instance ?: MailRepository().also { instance = it }
    }
    fun addMailEntry(mail: Mail) {
        db.collection(COLLECTION_NAME).document(mail.to).set(mail).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.w(TAG, "Successfully send welcome email to ${mail.to}")
            } else {
                Log.w(TAG, "Error while sending welcome email to ${mail.to}")
            }
        }
    }

}