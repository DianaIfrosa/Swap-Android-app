package com.diana.bachelorthesis.repository

import android.util.Log
import com.diana.bachelorthesis.model.Proposal
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Singleton

@Singleton
class ProposalRepository {
    private val TAG: String = ProposalRepository::class.java.name
    val db = Firebase.firestore

    val COLLECTION_NAME = "Proposals"

    companion object {
        @Volatile
        private var instance: ProposalRepository? = null
        fun getInstance() = instance ?: ProposalRepository().also { instance = it }
    }

    fun getProposalByID(id: String, callback: OneParamCallback<Proposal>) {
        db.collection(COLLECTION_NAME).document(id).get().addOnCompleteListener { task->
            if (task.isSuccessful) {
                val proposal = task.result.toObject(Proposal::class.java)
                Log.w(TAG, "Successfully retrieved proposal with id $id")
                callback.onComplete(proposal)
            } else {
                Log.w(TAG, "Error while retrieving proposal with id $id")
                callback.onError(task.exception)
            }
        }
    }

    fun createProposal(proposal: Proposal, callback: NoParamCallback) {
        db.collection(COLLECTION_NAME).document(proposal.proposalId).set(proposal).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Successfully added proposal with id ${proposal.proposalId}")
                callback.onComplete()
            } else {
                Log.w(TAG, "Failed to add proposal with id ${proposal.proposalId}")
                callback.onError(task.exception)
            }
        }
    }
}