package com.diana.bachelorthesis.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.PhotoRepository
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.NoParamCallback
import com.google.firebase.auth.FirebaseUser
import java.lang.Exception

class UserViewModel : ViewModel() {
    private val TAG: String = UserViewModel::class.java.name
    private val userRepository = UserRepository.getInstance()
    private val photosRepository = PhotoRepository.getInstance()

    fun verifyUserLoggedIn(): Boolean{
        return (userRepository.auth.currentUser != null)
    }

    fun getCurrentUser(): User {
        //TODO write current user to shared pref or some permanent storage
        return userRepository.currentUser
    }

    fun restoreCurrentUserData(callback:NoParamCallback) {
        userRepository.restoreCurrentUserData(callback)
    }

    fun registerUser(email: String, pass: String, callback: OneParamCallback<FirebaseUser>) {
        userRepository.registerUser(email, pass, callback)
    }

    fun addUser(email: String, name: String) {
        photosRepository.uploadProfilePhoto(email, null, object: OneParamCallback<String> {
            override fun onComplete(value: String?) {
                val user = User(email = email, name = name, profilePhoto = value)
                userRepository.addUser(user)
            }
            override fun onError(e: Exception?) {
                if (e != null) {
                    Log.d(TAG, e.message.toString())
                }
            }

        })
    }

    fun logInUser(email: String, pass: String, callback: OneParamCallback<FirebaseUser>) {
       userRepository.logInUser(email, pass, callback)
    }

    fun setCurrentUserData(email: String, callback: NoParamCallback) {
        userRepository.getUserData(email, object:OneParamCallback<User> {
            override fun onComplete(value: User?) {
                userRepository.currentUser = value!!
                callback.onComplete()
            }

            override fun onError(e: Exception?) {
               callback.onError(e)
            }

        })
    }

    fun signOut() {
        userRepository.auth.signOut()
    }
}