package com.diana.bachelorthesis.viewmodel

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.PhotoRepository
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.NoParamCallback
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import java.io.File
import kotlin.Exception

class UserViewModel : ViewModel() {
    private val TAG: String = UserViewModel::class.java.name
    private val userRepository = UserRepository.getInstance()
    private val photosRepository = PhotoRepository.getInstance()

    fun verifyUserLoggedIn(): Boolean {
        return (userRepository.auth.currentUser != null)
    }

    fun getCurrentUser(): User {
        //TODO write current user to shared pref or some permanent storage
        return userRepository.currentUser
    }

    fun restoreCurrentUserData(callback: NoParamCallback) {
        userRepository.restoreCurrentUserData(callback)
    }

    fun registerUser(email: String, pass: String, callback: OneParamCallback<FirebaseUser>) {
        userRepository.registerUser(email, pass, callback)
    }

    fun addUser(email: String, name: String, photoUri: Uri?, callback: NoParamCallback? = null) {
        photosRepository.uploadProfilePhoto(email, photoUri, object : OneParamCallback<String> {
            override fun onComplete(value: String?) {
                val user = User(email = email, name = name, profilePhoto = value)

                userRepository.addUser(user, callback)
            }

            override fun onError(e: Exception?) {
                if (e != null) {
                    Log.d(TAG, e.message.toString())
                }
                callback?.onError(e)
            }
        })
    }

    fun logInUser(email: String, pass: String, callback: OneParamCallback<FirebaseUser>) {
        userRepository.logInUser(email, pass, callback)
    }

    fun getCurrentUserEmail(): String {
        return userRepository.auth.currentUser!!.email!!
    }

    fun getCurrentUserName(): String {
        return userRepository.auth.currentUser!!.displayName!!
    }

    fun setCurrentUserData(email: String, callback: NoParamCallback) {
        userRepository.getUserData(email, object : OneParamCallback<User> {
            override fun onComplete(value: User?) {
                userRepository.currentUser = value!!
                callback.onComplete()
            }

            override fun onError(e: Exception?) {
                callback.onError(e)
            }

        })
    }

    fun getUserData(email: String, callback: OneParamCallback<User>) {
        userRepository.getUserData(email, callback)
    }

    fun signOut() {
        if (userRepository.auth.currentUser!!
                .providerData[userRepository.auth.currentUser!!.providerData.size -1]
                .providerId
                .equals("google.com", true)) {
            Log.d(TAG, "User was logged in with Google")
            userRepository.googleClient?.signOut()
        } else {
            Log.d(TAG, "User wasn't logged in with Google")
        }
        userRepository.auth.signOut()
    }

    fun signUpWithGoogle(credential: AuthCredential, callback: NoParamCallback) {
        userRepository.signUpWithGoogle(credential, callback)
    }

    fun getCurrentUserPhoto(): Uri? {
        return if (userRepository.auth.currentUser!!.photoUrl != null) {
            Uri.fromFile(File(userRepository.auth.currentUser!!.photoUrl.toString()))
        } else {
            null
        }
    }

    fun setGoogleClient(client: GoogleSignInClient) {
        userRepository.googleClient = client
    }

    fun getSignInIntentGoogle(): Intent {
        return userRepository.googleClient!!.signInIntent
    }
}