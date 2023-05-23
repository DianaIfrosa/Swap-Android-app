package com.diana.bachelorthesis.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback

class ForgotPassViewModel: ViewModel() {
    private val TAG: String = ForgotPassViewModel::class.java.name
    private val userRepository =  UserRepository.getInstance()

    fun verifyEmailIsInDb(email: String, callback: OneParamCallback<Boolean>) {
        Log.d(TAG, "Checking email $email is in database.")
        userRepository.verifyUserExists(email, callback)
    }

    fun sendResetPassEmail(email: String, callback: NoParamCallback) {
        Log.d(TAG, "Sending reset password email for email $email")
        userRepository.sendResetPassEmail(email, callback)
    }
}