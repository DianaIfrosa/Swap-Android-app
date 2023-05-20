package com.diana.bachelorthesis.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.*
import com.diana.bachelorthesis.repository.HistoryRepository
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.NoParamCallback

class HistoryViewModel : ViewModel() {
    private val TAG: String = HistoryViewModel::class.java.name

    private val historyRepository = HistoryRepository.getInstance()
    private val itemsRepository = ItemRepository.getInstance()

    var lastScrollPosition = 0

    var allItems: ArrayList<Item> = arrayListOf()
    var currentUserItems: ArrayList<Item> = arrayListOf()

    private var _availableItems = MutableLiveData<List<Item>>()
    private var _notAvailableItemsHistory = MutableLiveData<List<History>>()

    var availableItems: LiveData<List<Item>> = _availableItems
    val notAvailableItemsHistory: LiveData<List<History>> = _notAvailableItemsHistory

    lateinit var currentUser: User

    fun populateItemsAvailable() {
        getAllItems(object: NoParamCallback {
            override fun onComplete() {
                // retrieved all items
                getCurrentUserItems()
                getAvailableItems()
            }

            override fun onError(e: java.lang.Exception?) {}
        })
    }

    fun populateItemsNotAvailable() {
        getAllItems(object: NoParamCallback {
            override fun onComplete() {
                // retrieved all items
                getCurrentUserItems()
                getNotAvailableItemPairs()
            }

            override fun onError(e: java.lang.Exception?) {}
        })
    }

    private fun getAllItems(callback: NoParamCallback) {
        val items: ArrayList<Item> = arrayListOf()

        itemsRepository.getExchangeItems(object: ListParamCallback<Item> {
            override fun onComplete(values: ArrayList<Item>) {
                values.forEach {
                    items.add(it)
                }
                itemsRepository.getDonationItems(object: ListParamCallback<Item> {
                    override fun onComplete(values: ArrayList<Item>) {
                        values.forEach {
                            items.add(it)
                        }
                        allItems = items
                        callback.onComplete()
                    }

                    override fun onError(e: Exception?) {
                        allItems = items
                        callback.onComplete()
                    }
                })
            }

            override fun onError(e: Exception?) {
                itemsRepository.getDonationItems(object: ListParamCallback<Item> {
                    override fun onComplete(values: ArrayList<Item>) {
                        values.forEach {
                            items.add(it)
                        }
                        allItems = items
                        callback.onComplete()
                    }

                    override fun onError(e: Exception?) {
                        allItems = items
                        callback.onComplete()
                    }

                })
            }

        })
    }

    private fun getCurrentUserItems() {
        val result = arrayListOf<Item>()
        allItems.forEach { item ->
            if (item.owner == currentUser.email) {
                result.add(item)
            }
        }
        currentUserItems = result
    }

    private fun getAvailableItems() {
        val availableItemsUser = arrayListOf<Item>()

        currentUserItems.forEach { item ->
            if (item is ItemExchange) {
                if (item.exchangeInfo == null) {
                    availableItemsUser.add(item)
                }
            } else if (item is ItemDonation) {
                if (item.donationInfo == null) {
                    availableItemsUser.add(item)
                }
            }
        }

        _availableItems.value = availableItemsUser
    }

    fun getNotAvailableItemPairs() {
        val notAvailableItemsUser = arrayListOf<Item>()
        val historyIdsUser = arrayListOf<String>()

        currentUserItems.forEach { item ->
            if (item is ItemExchange) {
                if (item.exchangeInfo != null) {
                    notAvailableItemsUser.add(item)
                    historyIdsUser.add(item.exchangeInfo!!)
                }
            } else if (item is ItemDonation) {
                if (item.donationInfo != null) {
                    notAvailableItemsUser.add(item)
                    historyIdsUser.add(item.donationInfo!!)
                }
            }
        }

        Log.d(TAG, "Not available items of user:")
        notAvailableItemsUser.forEach {
            Log.d(TAG, it.name)
        }

        Log.d(TAG, "The history ids for them")
        historyIdsUser.forEach {
            Log.d(TAG, it)
        }

        historyRepository.getHistoryObjectsForUser(
            historyIdsUser,
            currentUser.email,
            object : ListParamCallback<History> {
                override fun onComplete(values: ArrayList<History>) {
                    _notAvailableItemsHistory.value = values
                }

                override fun onError(e: Exception?) {
                    _notAvailableItemsHistory.value = arrayListOf()
                }

            })
    }

}