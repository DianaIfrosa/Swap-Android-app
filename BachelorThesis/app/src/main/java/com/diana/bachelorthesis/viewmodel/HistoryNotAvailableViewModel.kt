package com.diana.bachelorthesis.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.*
import com.diana.bachelorthesis.repository.HistoryRepository
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.NoParamCallback

class HistoryNotAvailableViewModel: ViewModel() {
    private val TAG: String = HistoryNotAvailableViewModel::class.java.name

    private val historyRepository = HistoryRepository.getInstance()
    private val itemsRepository = ItemRepository.getInstance()

    lateinit var currentUser: User

    var allItems: ArrayList<Item> = arrayListOf()
    var currentUserItems: ArrayList<Item> = arrayListOf()
    var notAvailableItemsHistory: ArrayList<History> = arrayListOf()
    var itemsPairs: ArrayList<Pair<Item, Item?>> = arrayListOf()

    fun populateItemsNotAvailable(callback: NoParamCallback) {
        getAllItems(object: NoParamCallback {
            override fun onComplete() {
                // retrieved all items
                getCurrentUserItems()
                getNotAvailableItemPairs(object: ListParamCallback<History> {
                    override fun onComplete(values: ArrayList<History>) {
                        Log.d(TAG, "Retrieved history events")
                        val histories = values
                        if (values.size != notAvailableItemsHistory.size || (values.size == 0 && notAvailableItemsHistory.size == 0)) {
                            if (histories.isNotEmpty()) {
                                itemsRepository.getHistoryItems(0, histories, arrayListOf(), object: ListParamCallback<Pair<Item, Item?>> {
                                    override fun onComplete(values: ArrayList<Pair<Item, Item?>>) {
                                        itemsPairs = values
                                        notAvailableItemsHistory = histories
                                        Log.d(TAG, "oncomplete, should update recycler view")
                                        callback.onComplete()
                                    }

                                    override fun onError(e: Exception?) {
                                       callback.onError(e)
                                    }

                                })
                            } else {
                                Log.d(TAG, "No history events, should update recycler view")
                                notAvailableItemsHistory = arrayListOf()
                                itemsPairs = arrayListOf()
                                callback.onComplete()
                            }
                        } else
                            Log.d(TAG, "History events retrieved are not different from the ones already existing.")
                    }

                    override fun onError(e: Exception?) {
                            callback.onError(e)
                    }
                })
            }

            override fun onError(e: Exception?) {
                callback.onError(e)
            }
        })
    }


    fun getNotAvailableItemPairs(callback: ListParamCallback<History>) {
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
                    Log.d(TAG, "Retrieved ${values.size} history objects from DB.")
                    callback.onComplete(values)
                }

                override fun onError(e: Exception?) {
                    Log.d(TAG, "Retrieved error from DB.")
                    callback.onError(e)
                }

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

}