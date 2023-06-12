package com.diana.bachelorthesis.repository

import android.util.Log
import com.diana.bachelorthesis.model.History
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.NoParamCallback
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Singleton

@Singleton
class HistoryRepository {
    private val TAG: String = HistoryRepository::class.java.name
    val db = Firebase.firestore

    private val COLLECTION_NAME = "History"

    companion object {
        @Volatile
        private var instance: HistoryRepository? = null
        fun getInstance() = instance ?: HistoryRepository().also { instance = it }
    }

    fun getHistoryObjectsForUser(historyIds: List<String>, userEmail: String, callback: ListParamCallback<History>) {
        val resultList = arrayListOf<History>()

        db.collection(COLLECTION_NAME).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val results = task.result
                results.forEach {
                    val history = it.toObject(History::class.java)
                    if (history.eventId in historyIds) {
                        // current user exchanged or donated something
                        resultList.add(history)
                    } else if (history.donationReceiverEmail == userEmail) {
                        // the current user received a donation
                        resultList.add(history)
                    }
                }
                Log.d(TAG, "Successfully retrieved history objects for user $userEmail")
                callback.onComplete(resultList)
            } else {
                Log.w(TAG, "Error while retrieving history objects")
                if (task.exception != null) {
                    Log.w(TAG, task.exception!!.message.toString())
                }
            }
        }
    }

    fun addHistory(history: History, callback: NoParamCallback) {
        db.collection(COLLECTION_NAME).document(history.eventId).set(history).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.w(TAG, "Successfully added history with id ${history.eventId}.")
                callback.onComplete()
            } else {
                Log.w(TAG, "Error occurred while adding history with id ${history.eventId}.")
                callback.onError(task.exception)
            }
        }
    }
}