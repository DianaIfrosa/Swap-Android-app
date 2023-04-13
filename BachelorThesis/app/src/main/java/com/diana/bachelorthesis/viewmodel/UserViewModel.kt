package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.UserRepository
import com.google.firebase.auth.FirebaseUser

class UserViewModel : ViewModel() {
    private val TAG: String = UserViewModel::class.java.name
    private val userRepository = UserRepository.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return userRepository.auth.currentUser
    }

    fun registerUser(email: String, pass: String, callback: OneParamCallback<FirebaseUser>) {
        userRepository.registerUser(email, pass, callback)
    }

    fun addUser(email: String, name: String) {
        val user = User(email = email, name = name)
        userRepository.addUser(user)
    }

    fun signInUser(email: String, pass: String, callback: OneParamCallback<FirebaseUser>) {
       userRepository.signInUser(email, pass, callback)
    }

    fun signOut() {
        userRepository.auth.signOut()
    }

}