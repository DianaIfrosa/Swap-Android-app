package com.diana.bachelorthesis.repository

import android.util.Log
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.model.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.Exception
import kotlin.math.ceil

class UserRepository {

    private val TAG: String = UserRepository::class.java.name
    val db = Firebase.firestore
    val auth = Firebase.auth
    var currentUser: User = User("Loading email", "Loading name") // TODO make string res

    // for cloud
    val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageReference: StorageReference = firebaseStorage.reference

    val COLLECTION_NAME = "Users"

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance() =
            instance ?: UserRepository().also { instance = it }
    }

    init {
        if (auth.currentUser != null) {
            restoreCurrentUserData()
        }
    }

    private fun restoreCurrentUserData() {
        getUserData(auth.currentUser!!.email!!, object: OneParamCallback<User>{
            override fun onComplete(value: User?) {
                currentUser = value!!
                Log.d(TAG, "Restored current user's data!")
            }

            override fun onError(e: Exception?) {
                Log.w(TAG, "Could not restore current user's data, see error below:")
                if (e != null) {
                    e.message?.let { Log.w(TAG, it) }
                }
            }
        })
    }

    fun addUser(userToAdd: User) {
        db.collection(COLLECTION_NAME)
            .document(userToAdd.email)
            .set(userToAdd)
            .addOnSuccessListener {
                Log.d(TAG, "Successful addition of DocumentSnapshot with id ${userToAdd.email}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document ${e.message}")
            }
    }

    fun registerUser(email: String, pass: String, callback: OneParamCallback<FirebaseUser>) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.w(TAG, "Register successful for user: $email")
                    callback.onComplete(auth.currentUser)
                } else {
                    Log.w(TAG, "Register failed for user: $email")
                    callback.onError(task.exception)
                }
            }
    }

    fun logInUser(email: String, pass: String, callback: OneParamCallback<FirebaseUser>) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail successful for user $email")
                    callback.onComplete(auth.currentUser)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail failed fro user $email", task.exception)
                    callback.onError(task.exception)
                }
            }
    }

    fun getUserData(email: String, callback: OneParamCallback<User>){
        db.collection(COLLECTION_NAME)
            .document(email)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null) {
                        val user = document.toObject(User::class.java)
                        callback.onComplete(user)
                    } else {
                        callback.onError(task.exception)
                    }
                } else {
                    Log.w(TAG, "Error while retrieving user $email, see message below")
                    if (task.exception != null) {
                        Log.w(TAG, ", ${task.exception!!.message}")
                    }
                    callback.onError(task.exception)
                }
            }
    }
}