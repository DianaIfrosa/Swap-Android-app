package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.History
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback
import java.lang.Exception

class DonationEventViewModel: ViewModel() {

    private val TAG: String = DonationEventViewModel::class.java.name
    private val userRepository = UserRepository.getInstance()

    lateinit var item1: Item
    var item2: Item? = null

    private var _donationReceiver = MutableLiveData<User?>()
    var donationReceiver: LiveData<User?> = _donationReceiver

    lateinit var history: History
    lateinit var currentUser: User

    fun getDonationReceiverData() {
        userRepository.getUserData(history.donationReceiverEmail!!, object: OneParamCallback<User> {
            override fun onComplete(value: User?) {
                if (value != null) {
                    _donationReceiver.value = value
                }
            }

            override fun onError(e: Exception?) {
                _donationReceiver.value = null
            }
        })
    }

}