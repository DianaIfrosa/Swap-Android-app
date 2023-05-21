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

class ExchangeEventViewModel: ViewModel() {
    private val TAG: String = ExchangeEventViewModel::class.java.name
    private val userRepository = UserRepository.getInstance()

    lateinit var item1: Item
    var item2: Item? = null

    private var _otherOwner = MutableLiveData<User?>()
    var otherOwner: LiveData<User?> = _otherOwner

    lateinit var history: History
    lateinit var currentUser: User

    fun swapItems() {
        val itemAux: Item = item1
        item1 = item2!!
        item2 = itemAux
    }

    fun getOtherOwnerData() {
        userRepository.getUserData(item2!!.owner, object: OneParamCallback<User> {
            override fun onComplete(value: User?) {
                if (value != null) {
                    _otherOwner.value = value
                }
            }

            override fun onError(e: Exception?) {
               _otherOwner.value = null
            }
        })
    }
}