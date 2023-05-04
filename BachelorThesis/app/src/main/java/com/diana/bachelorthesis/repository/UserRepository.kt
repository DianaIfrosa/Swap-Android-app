package com.diana.bachelorthesis.repository

import android.util.Log
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.utils.NoParamCallback
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class UserRepository {

    val TAG: String = UserRepository::class.java.name
    private val db = Firebase.firestore
    val auth = Firebase.auth

    var googleClient: GoogleSignInClient? = null

    // for cloud
    val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    val COLLECTION_NAME = "Users"

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance() =
            instance ?: UserRepository().also {
                instance = it
                Log.d("UserRepository", "UserRepository has been created.")
            }
    }

    fun addOrUpdateUser(userToAdd: User, callback: NoParamCallback? = null) {
        db.collection(COLLECTION_NAME)
            .document(userToAdd.email)
            .set(userToAdd)
            .addOnSuccessListener {
                Log.d(TAG, "Successful addition or update of DocumentSnapshot with id ${userToAdd.email}")
                callback?.onComplete()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding or updating document ${e.message}")
                callback?.onError(e)
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

    fun signUpWithGoogle(credential: AuthCredential, callback: NoParamCallback) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback.onComplete()
            } else {
                Log.w(TAG, "Error while signing up with Google, see message below")
                if (task.exception != null) {
                    Log.w(TAG, task.exception!!.message.toString())
                }
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
                    Log.w(TAG, "signInWithEmail failed for user $email", task.exception)
                    callback.onError(task.exception)
                }
            }
    }

    fun getUserData(email: String, callback: OneParamCallback<User>) {
        db.collection(COLLECTION_NAME)
            .document(email)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null) {
                        val user = document.toObject(User::class.java)
                        Log.d(TAG, "Retrieved user's data for email $email")
                        callback.onComplete(user)
                    } else {
                        callback.onError(task.exception)
                    }
                } else {
                    Log.w(TAG, "Error while retrieving user $email data, see message below")
                    if (task.exception != null) {
                        Log.w(TAG, task.exception!!.message.toString())
                    }
                    callback.onError(task.exception)
                }
            }
    }

}