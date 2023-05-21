package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.History
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.OneParamCallback
import java.lang.Exception

class ReceiveDonationEventViewModel: ViewModel() {
    private val TAG: String = ReceiveDonationEventViewModel::class.java.name
    private val userRepository = UserRepository.getInstance()

    lateinit var item1: Item
    var item2: Item? = null

    private var _donationMaker = MutableLiveData<User?>()
    var donationMaker: LiveData<User?> = _donationMaker

    lateinit var history: History
    lateinit var currentUser: User

    fun getDonationMakerData() {
        userRepository.getUserData(item1.owner, object: OneParamCallback<User> {
            override fun onComplete(value: User?) {
                if (value != null) {
                    _donationMaker.value = value
                }
            }

            override fun onError(e: Exception?) {
              _donationMaker.value = null
            }
        })
    }
}