package com.diana.bachelorthesis.repository

import android.util.Log
import com.diana.bachelorthesis.model.PostReport
import com.diana.bachelorthesis.utils.NoParamCallback
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Singleton

@Singleton
class ReportsRepository {

    private val TAG: String = ReportsRepository::class.java.name
    val db = Firebase.firestore

    val COLLECTION_NAME = "Reports"
    companion object {
        @Volatile
        private var instance: ReportsRepository? = null
        fun getInstance() = instance ?: ReportsRepository().also { instance = it }
    }


    fun addReport(report: PostReport, callback: NoParamCallback) {
        db.collection(COLLECTION_NAME).document(report.id).set(report).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.w(TAG, "Successful addition of  post report with id ${report.id}")
                callback.onComplete()
            } else {
                Log.w(TAG, "Error while adding post report")
                callback.onError(task.exception)
            }
        }
    }
}