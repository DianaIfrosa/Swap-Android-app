package com.diana.bachelorthesis.repository

import android.util.Log
import com.diana.bachelorthesis.model.History
import com.diana.bachelorthesis.utils.ListParamCallback
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Singleton

@Singleton
class HistoryRepository {
    private val TAG: String = HistoryRepository::class.java.name
    val db = Firebase.firestore

    val COLLECTION_NAME = "History"

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
                callback.onComplete(resultList)
            } else {
                Log.w(TAG, "Error while retrieving history objects")
                if (task.exception != null) {
                    Log.w(TAG, task.exception!!.message.toString())
                }
            }
        }
    }
}