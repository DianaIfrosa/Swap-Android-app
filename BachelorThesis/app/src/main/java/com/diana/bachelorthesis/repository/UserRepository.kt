package com.diana.bachelorthesis.repository

import android.util.Log
import com.diana.bachelorthesis.model.*
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.NoParamCallback
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Singleton

@Singleton
class UserRepository {

    val TAG: String = UserRepository::class.java.name
    private val db = Firebase.firestore
    val auth = Firebase.auth

    var googleClient: GoogleSignInClient? = null
    private val COLLECTION_NAME = "Users"
    private var currentUserListener: ListenerRegistration? = null

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance() =
            instance ?: UserRepository().also {
                instance = it
                Log.d("UserRepository", "UserRepository has been created.")
            }
    }

    fun deleteUser() {
        auth.currentUser?.delete()
    }

    fun addOrUpdateUser(userToAdd: User, callback: NoParamCallback? = null) {
        db.collection(COLLECTION_NAME)
            .document(userToAdd.email)
            .set(userToAdd)
            .addOnSuccessListener {
                Log.d(
                    TAG,
                    "Successful addition or update of DocumentSnapshot with id ${userToAdd.email}"
                )
                callback?.onComplete()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding or updating document ${e.message}")
                callback?.onError(e)
            }
    }

    fun sendResetPassEmail(email: String, callback: NoParamCallback) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Successfully sent reset pass email to $email")
                callback.onComplete()
            }
            else {
                Log.w(TAG, "Error while sending reset pass email to $email")
                Log.w(TAG, task.exception)
                callback.onError(task.exception)
            }
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
                Log.w(TAG, "Error while signing in with Google")
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
                    Log.w(TAG, "Error while retrieving user $email data")
                    if (task.exception != null) {
                        Log.w(TAG, task.exception!!.message.toString())
                    }
                    callback.onError(task.exception)
                }
            }
    }

    fun getUsersEmailsFromNames(names: ArrayList<String>, callback: ListParamCallback<String>) {
        db.collection(COLLECTION_NAME).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Retrieved all users.")
                val allUsersEmails = mutableListOf<String>()
                task.result.forEach {
                    val user = it.toObject(User::class.java)
                    names.forEach verifyNames@{ text ->
                        val words = text.split(" ")
                        if (words.all { word ->
                                user.name.contains(word, true)
                            }) {
                            allUsersEmails.add(user.email)
                            return@verifyNames
                        }
                    }
                }
                callback.onComplete(allUsersEmails as ArrayList<String>)
            } else {
                Log.w(
                    TAG,
                    "Error while retrieving all users"
                )
                if (task.exception != null) {
                    Log.w(TAG, task.exception!!.message.toString())
                }
                callback.onError(task.exception)
            }
        }

    }
    fun getAllUsersName(callback: ListParamCallback<String>) {
        db.collection(COLLECTION_NAME).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Retrieved all users.")
                val allUsersNames = mutableListOf<String>()
                task.result.forEach {
                    val user = it.toObject(User::class.java)
                    allUsersNames.add(user.name)
                }
                callback.onComplete(allUsersNames as ArrayList<String>)
            } else {
                Log.w(
                    TAG,
                    "Error while retrieving all users"
                )
                if (task.exception != null) {
                    Log.w(TAG, task.exception!!.message.toString())
                }
                callback.onError(task.exception)
            }
        }
    }

    fun changeUserNotificationOption(userEmail: String, option: Int) {
        db.collection(COLLECTION_NAME)
            .document(userEmail)
            .update("notifications.notificationsOption", option)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Updated user notifications option for email $userEmail")
                } else {
                    Log.w(
                        TAG,
                        "Error while updating user $userEmail notifications option"
                    )
                    if (task.exception != null) {
                        Log.w(TAG, task.exception!!.message.toString())
                    }
                }
            }
    }

    fun changeUserPreferences(
        userEmail: String,
        words: List<String>,
        owners: List<String>,
        cities: List<String>,
        categories: List<ItemCategory>,
        exchangePreferences: List<ItemCategory>
    ) {
        db.collection(COLLECTION_NAME)
            .document(userEmail)
            .update(
                "notifications.preferredWords", words,
                "notifications.preferredOwners", owners,
                "notifications.preferredCities", cities,
                "notifications.preferredCategories", categories,
                "notifications.preferredExchangePreferences", exchangePreferences
            )
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Updated user preferences for recommendations for email $userEmail")
                } else {
                    Log.w(
                        TAG,
                        "Error while updating user $userEmail preferences for recommendations"
                    )
                    if (task.exception != null) {
                        Log.w(TAG, task.exception!!.message.toString())
                    }
                }
            }
    }

    fun updatePassword(newPass: String, callback: NoParamCallback) {
        auth.currentUser!!.updatePassword(newPass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Password updated successfully")
                callback.onComplete()
            } else {
                Log.d(TAG, "Password couldn't be updated")
                callback.onError(task.exception)
            }
        }
    }

    fun verifyUserExists(email: String, callback: OneParamCallback<Boolean>) {
        db.collection(COLLECTION_NAME).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var answer = false
                Log.d(TAG, "Retrieved all users to verify if some user exists already")
                task.result.forEach verifyUserEmails@{
                    val user = it.toObject(User::class.java)
                   if (user.email == email) {
                       answer = true
                       return@verifyUserEmails
                   }
                }
                callback.onComplete(answer)

            } else {
                Log.w(
                    TAG,
                    "Error while retrieving all users to verify if some user exists already"
                )
                if (task.exception != null) {
                    Log.w(TAG, task.exception!!.message.toString())
                }
                callback.onError(task.exception)
            }
        }
    }

    fun getUsersByEmail(emails: List<String>, currentPosition: Int, result: ArrayList<User?>, callback:ListParamCallback<User?>){
        db.collection(COLLECTION_NAME).document(emails[currentPosition]).get().addOnCompleteListener {task->
            if (task.isSuccessful) {
                val user = task.result.toObject(User::class.java)
                result.add(user)
                if (currentPosition == emails.size - 1) {
                    callback.onComplete(result)
                } else {
                    getUsersByEmail(emails, currentPosition + 1,  result, callback)
                }
            } else {
                Log.w(TAG, "Error retrieving users recursively")
                callback.onError(task.exception)
            }
        }
    }

    fun listenToCurrentUserChanges(email:String, callback: OneParamCallback<User>) {
        currentUserListener = db.collection(COLLECTION_NAME).document(email).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed for current user", error)
                callback.onError(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val user = snapshot.toObject(User::class.java)
                callback.onComplete(user)
            } else {
                Log.w(TAG, "No such snapshot")
            }
        }
    }

    fun addChatIdToUserList(chatId: String, userEmail: String) {
        val chatNew = hashMapOf(
            "chatId" to chatId,
            "seen" to "false"
        )
        db.collection(COLLECTION_NAME).document(userEmail).update(
          "chatIds", FieldValue.arrayUnion(chatNew)
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Successfully added new chat id $chatId to user $userEmail")
            } else {
                Log.w(TAG, "Error occurred while adding new chat id $chatId to user $userEmail")
            }

        }
    }

    fun detachCurrentUserListener() {
        currentUserListener?.remove()
    }

}