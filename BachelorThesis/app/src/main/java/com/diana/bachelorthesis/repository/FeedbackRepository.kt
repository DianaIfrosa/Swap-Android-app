package com.diana.bachelorthesis.repository

import android.util.Log
import com.diana.bachelorthesis.model.Feedback
import com.diana.bachelorthesis.utils.NoParamCallback
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Singleton

@Singleton
class FeedbackRepository {
    private val TAG: String = FeedbackRepository::class.java.name
    private val db = Firebase.firestore
    private val COLLECTION_NAME = "Feedback"

    companion object {
        @Volatile
        private var instance: FeedbackRepository? = null

        fun getInstance() = instance ?: FeedbackRepository().also { instance = it }
    }

    fun addFeedback(feedback: Feedback, callback: NoParamCallback) {
        db.collection(COLLECTION_NAME).document(feedback.id).set(feedback)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(
                        TAG,
                        "Successfully added feedback with id ${feedback.id} from user ${feedback.senderEmail}"
                    )
                    callback.onComplete()

                } else {
                    Log.w(
                        TAG,
                        "Error while adding feedback with id ${feedback.id} from user ${feedback.senderEmail}"
                    )
                    Log.w(TAG, task.exception)
                    callback.onError(task.exception)
                }

            }

    }

}