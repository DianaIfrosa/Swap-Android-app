package com.diana.bachelorthesis.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.OnCompleteCallback
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemDonation
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.repository.ItemRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


class HomeViewModel : ViewModel() {
    private val TAG: String = HomeViewModel::class.java.getName()
    private val _text = MutableLiveData<String>().apply {
        value = "15 produse"
    }

    val text: LiveData<String> = _text

    private val _exchangeItems = MutableLiveData<List<Item>>()
    private val _donationItems = MutableLiveData<List<Item>>()

    var exchangeItems: LiveData<List<Item>> = _exchangeItems
    val donationItems: LiveData<List<Item>> = _donationItems

    val repository = ItemRepository.getInstance()

    init {
        populateLiveData()
    }

    private fun populateLiveData() {
        //_exchangeItems.value = repository.getItems(true)
        //_donationItems.value = repository.getItems(false)

        repository.getItems(true, object: OnCompleteCallback {
            override fun onCompleteGetItems(items: ArrayList<Item>) {
                _exchangeItems.value = items
            }
        })

        repository.getItems(false, object: OnCompleteCallback {
            override fun onCompleteGetItems(items: ArrayList<Item>) {
                _donationItems.value = items
            }
        })
    }

}
