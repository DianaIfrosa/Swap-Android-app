package com.diana.bachelorthesis.repository

import android.util.Log
import com.diana.bachelorthesis.model.Mail
import com.diana.bachelorthesis.utils.NoParamCallback
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Singleton

@Singleton
class MailRepository {

    private val TAG: String = MailRepository::class.java.name
    val db = Firebase.firestore
    private val COLLECTION_NAME = "Mail"

    companion object {
        @Volatile
        private var instance: MailRepository? = null
        fun getInstance() = instance ?: MailRepository().also { instance = it }
    }

    fun addMailEntry(mail: Mail, callback: NoParamCallback) {
        Log.d(TAG, "in addMailEntry method")
        db.collection(COLLECTION_NAME).document(mail.to).set(mail).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.w(TAG, "Successfully send email to ${mail.to}")
                callback.onComplete()
            } else {
                Log.w(TAG, "Error while sending email to ${mail.to}")
                callback.onError(task.exception)
            }
        }.addOnCanceledListener {
            Log.w(TAG, "Canceled sending email to ${mail.to}")
            callback.onError(null)
        }
    }
}