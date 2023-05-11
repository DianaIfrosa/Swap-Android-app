package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.*
import com.diana.bachelorthesis.repository.HistoryRepository
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.NoParamCallback

class HistoryViewModel : ViewModel() {
    private val TAG: String = HistoryViewModel::class.java.name

    private val historyRepository = HistoryRepository.getInstance()
    private val itemsRepository = ItemRepository.getInstance()

    var lastScrollPosition = 0

    var allItems: ArrayList<Item> = arrayListOf()
    var currentUserItems: ArrayList<Item> = arrayListOf()
    var historyList: ArrayList<History> = arrayListOf()

    var _availableItems = MutableLiveData<List<Item>>()
    var _notAvailableItems = MutableLiveData<List<Pair<Item, Item?>>>()

    var availableItems: LiveData<List<Item>> = _availableItems
    val notAvailableItems: LiveData<List<Pair<Item, Item?>>> = _notAvailableItems

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
        allItems.forEach { item ->
            if (item.owner == currentUser.email) {
                currentUserItems.add(item)
            }
        }
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

        historyRepository.getHistoryObjectsForUser(
            historyIdsUser,
            currentUser.email,
            object : ListParamCallback<History> {
                override fun onComplete(values: ArrayList<History>) {
                    historyList = values
                    val correspondingItems = getCorrespondingItems(notAvailableItemsUser, values)
                    _notAvailableItems.value = notAvailableItemsUser.zip(correspondingItems)
                }

                override fun onError(e: Exception?) {
                    _notAvailableItems.value = arrayListOf()
                }

            })
    }

    private fun getCorrespondingItems(itemsIds: ArrayList<Item>, historyList: ArrayList<History>): ArrayList<Item?> {
        val resultList = arrayListOf<Item?>()

        historyList.zip(itemsIds).forEach { (history, item) ->
            if (history.item2 == null) { // donation
                resultList.add(null)
            } else { // exchange

                if (history.item1 == item.itemId) {
                    // search for the second item
                    val secondItem = allItems.find {currentItem ->
                        currentItem.itemId == history.item2
                    }
                    resultList.add(secondItem)
                } else { // search for the first item
                    val secondItem = allItems.find {currentItem ->
                        currentItem.itemId == history.item1
                    }
                    resultList.add(secondItem)
                }
            }
        }

        return resultList
    }

}