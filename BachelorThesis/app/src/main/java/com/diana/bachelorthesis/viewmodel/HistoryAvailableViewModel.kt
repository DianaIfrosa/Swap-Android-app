package com.diana.bachelorthesis.viewmodel

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
    var currentUserItems: ArrayList<Item> = arrayListOf()
    var availableItems: ArrayList<Item> = arrayListOf()

    fun populateItemsAvailable(callback: NoParamCallback) {
        getAllItems(object: NoParamCallback {
            override fun onComplete() {
                // retrieved all items
                getCurrentUserItems()
                getAvailableItems()
                callback.onComplete()
            }

            override fun onError(e: java.lang.Exception?) {
                    callback.onError(e)
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

        availableItems = availableItemsUser
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