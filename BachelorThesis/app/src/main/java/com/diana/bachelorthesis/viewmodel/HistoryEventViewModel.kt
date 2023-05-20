package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.History
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback
import java.lang.Exception

class HistoryEventViewModel: ViewModel() {

    private val TAG: String = HistoryEventViewModel::class.java.name
    private val userRepository = UserRepository.getInstance()

    lateinit var item1: Item
    var item2: Item? = null

    lateinit var donationReceiver: User

    lateinit var otherOwner: User

    lateinit var history: History
    lateinit var currentUser: User

    fun swapItems() {
        val itemAux: Item = item1
        item1 = item2!!
        item2 = itemAux

    }

    fun getOtherOwnerData(callback: NoParamCallback) {
        userRepository.getUserData(item2!!.owner, object: OneParamCallback<User> {
            override fun onComplete(value: User?) {
                if (value != null) {
                    otherOwner = value
                    callback.onComplete()
                }
            }

            override fun onError(e: Exception?) {
               callback.onError(e)
            }
        })
    }

    fun getDonationReceiverData(callback: NoParamCallback) {
        userRepository.getUserData(history.donationReceiverEmail!!, object: OneParamCallback<User> {
            override fun onComplete(value: User?) {
                if (value != null) {
                    donationReceiver = value
                    callback.onComplete()
                }
            }

            override fun onError(e: Exception?) {
                callback.onError(e)
            }
        })
    }

    fun getDonationMakerData(callback: NoParamCallback) {
        userRepository.getUserData(item1.owner, object: OneParamCallback<User> {
            override fun onComplete(value: User?) {
                if (value != null) {
                    otherOwner = value
                    callback.onComplete()
                }
            }

            override fun onError(e: Exception?) {
                callback.onError(e)
            }
        })
    }
}