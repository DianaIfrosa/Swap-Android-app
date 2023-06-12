package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemDonation
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.NoParamCallback

class HistoryAvailableViewModel: ViewModel() {
    private val TAG: String = HistoryAvailableViewModel::class.java.name

    private val itemsRepository = ItemRepository.getInstance()

    lateinit var currentUser: User
    var allItems: ArrayList<Item> = arrayListOf()
    private var currentUserItems: ArrayList<Item> = arrayListOf()
    private var _availableItems =  MutableLiveData<ArrayList<Item>?>()
    var availableItems: LiveData<ArrayList<Item>?> = _availableItems

    fun populateItemsAvailable() {
        getAllItems(object: NoParamCallback {
            override fun onComplete() {
                // retrieved all items
                getCurrentUserItems()
                _availableItems.value = getAvailableItems()
            }

            override fun onError(e: java.lang.Exception?) {
                _availableItems.value = null
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
        currentUserItems = ArrayList(result.sortedByDescending { it.postDate })
    }

    private fun getAvailableItems(): ArrayList<Item> {
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

        return availableItemsUser
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

}