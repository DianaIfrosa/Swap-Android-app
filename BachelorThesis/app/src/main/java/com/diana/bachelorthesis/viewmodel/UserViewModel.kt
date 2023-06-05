package com.diana.bachelorthesis.viewmodel

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.ItemCategory
import com.diana.bachelorthesis.model.Mail
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.*
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.NoParamCallback
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import java.io.File
import kotlin.Exception

class UserViewModel : ViewModel() {
    private val TAG: String = UserViewModel::class.java.name

    private val userRepository = UserRepository.getInstance()
    private val userTokensRepository = UserTokensRepository.getInstance()
    private val photosRepository = PhotoRepository.getInstance()
    private val mailRepository = MailRepository.getInstance()
    private val chatRepository = ChatRepository.getInstance()

    fun verifyUserLoggedIn(): Boolean {
        return (userRepository.auth.currentUser != null)
    }

    fun deleteUser() {
        userRepository.deleteUser()
    }

    fun registerUser(email: String, pass: String, callback: OneParamCallback<FirebaseUser>) {
        userRepository.registerUser(email, pass, callback)
    }

    fun sendWelcomeEmail(mail: Mail) {
        Log.d(TAG, "Sending welcome email")
        mailRepository.addMailEntry(mail)
    }

    fun addUser(email: String, name: String, photoUri: Uri?, callback: NoParamCallback? = null) {
        photosRepository.uploadProfilePhoto(email, photoUri, object : OneParamCallback<String> {
            override fun onComplete(value: String?) {
                val user = User(email = email, name = name, profilePhoto = value)

                userRepository.addOrUpdateUser(user, callback)
            }

            override fun onError(e: Exception?) {
                if (e != null) {
                    Log.d(TAG, e.message.toString())
                }
                callback?.onError(e)
            }
        })
    }

    fun updateCurrentUser(user: User) {
        userRepository.addOrUpdateUser(user)
    }

    fun logInUser(email: String, pass: String, callback: OneParamCallback<FirebaseUser>) {
        userRepository.logInUser(email, pass, callback)
    }

    fun getCurrentUserEmail(): String {
        return userRepository.auth.currentUser!!.email!!
    }

    fun addUserToken(email: String, token: String){
        userTokensRepository.addUserToken(email, token)
    }

    fun getCurrentUserName(): String {
        return userRepository.auth.currentUser!!.displayName!!
    }


    fun getUserData(email: String, callback: OneParamCallback<User>) {
        userRepository.getUserData(email, callback)
    }

    fun getAllUsersName(callback: ListParamCallback<String>) {
        userRepository.getAllUsersName(callback)
    }

    fun userLoggedInWithGoogle(): Boolean {
        return userRepository.googleClient != null
    }

    fun signOut(email: String) {
        chatRepository.detachChatListeners()
        userRepository.detachCurrentUserListener()

        userTokensRepository.deleteToken(email)

        userRepository.auth.signOut()
        userRepository.googleClient?.signOut()
        userRepository.googleClient = null
        if (userLoggedInWithGoogle()) {
            Log.d(TAG, "User was logged in with Google")
        } else {
            Log.d(TAG, "User wasn't logged in with Google")
        }
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

    fun changeUserNotificationsOption(userEmail: String, option: Int) {
        userRepository.changeUserNotificationOption(userEmail, option)
    }

    fun changeUserPreferences(
        userEmail: String,
        words: List<String>,
        owners: List<String>,
        cities: List<String>,
        categories: List<ItemCategory>,
        exchangePreferences: List<ItemCategory>
    ) {
        userRepository.changeUserPreferences(
            userEmail,
            words,
            owners,
            cities,
            categories,
            exchangePreferences
        )
    }

    fun updatePassword(newPass: String, callback: NoParamCallback) {
        userRepository.updatePassword(newPass, callback)
    }

    fun verifyUserExists(email: String, callback: OneParamCallback<Boolean>) {
       userRepository.verifyUserExists(email, callback)
    }
}