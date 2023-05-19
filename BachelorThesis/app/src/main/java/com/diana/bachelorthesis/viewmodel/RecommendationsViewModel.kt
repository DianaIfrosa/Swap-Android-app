package com.diana.bachelorthesis.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemDonation
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.ListParamCallback

class RecommendationsViewModel : ViewModel() {
    private val TAG: String = RecommendationsViewModel::class.java.name
    private val itemRepository = ItemRepository.getInstance()
    private val userRepository = UserRepository.getInstance()

    private val _items = MutableLiveData<List<Item>>()
    var items: LiveData<List<Item>> = _items

    private var itemsRecommended: ArrayList<Item> = arrayListOf()

    lateinit var currentUser: User
    var lastScrollPosition = 0

    private fun getDonationItems(callback: ListParamCallback<Item>) {
        itemRepository.getDonationItems(object : ListParamCallback<Item> {
            override fun onComplete(values: ArrayList<Item>) {
                Log.d(TAG, "Retrieved recommended donation items")
                val valuesFiltered = ArrayList(values.filter { item ->
                    when (item) {
                        is ItemExchange -> {
                            (item.exchangeInfo == null) && (currentUser.email != item.owner)
                        }
                        is ItemDonation -> {
                            (item.donationInfo == null) && (currentUser.email != item.owner)
                        }
                        else -> false
                    }
                })

                callback.onComplete(valuesFiltered)
            }

            override fun onError(e: Exception?) {
                callback.onComplete(arrayListOf())
            }
        })
    }

    private fun getExchangeItems(callback: ListParamCallback<Item>) {
        itemRepository.getExchangeItems(object : ListParamCallback<Item> {
            override fun onComplete(values: ArrayList<Item>) {
                Log.d(TAG, "Retrieved recommended exchange items")
                val valuesFiltered = ArrayList(values.filter { item ->
                    when (item) {
                        is ItemExchange -> {
                            (item.exchangeInfo == null)
                        }
                        is ItemDonation -> {
                            (item.donationInfo == null)
                        }
                        else -> false
                    }
                })
                callback.onComplete(valuesFiltered)
            }

            override fun onError(e: Exception?) {
                callback.onComplete(arrayListOf())
            }
        })
    }

    fun populateLiveDataItems() {
        Log.d(TAG, "Populating live data items in RecommendationsViewModel.")

        getDonationItems(object : ListParamCallback<Item> {
            override fun onComplete(values: ArrayList<Item>) {
                getExchangeItemsAfterDonation(values)
            }

            override fun onError(e: Exception?) {
                getExchangeItemsAfterDonation(arrayListOf())
            }
        })
    }

    private fun getExchangeItemsAfterDonation(donationItems: ArrayList<Item>) {
        getExchangeItems(object : ListParamCallback<Item> {
            override fun onComplete(values: ArrayList<Item>) {
                filterAndShuffleItems(donationItems, values)
            }

            override fun onError(e: Exception?) {
                filterAndShuffleItems(donationItems, arrayListOf())
            }
        })
    }

    private fun filterAndShuffleItems(
        donationItems: ArrayList<Item>,
        exchangeItems: ArrayList<Item>
    ) {
        Log.d(TAG, "Filtering and shuffling items.")
        val allItems = donationItems + exchangeItems

        if (currentUser.notifications.preferredOwners.isNotEmpty()) {
            userRepository.getUsersEmailsFromNames(
                ArrayList(currentUser.notifications.preferredOwners),
                object : ListParamCallback<String> {
                    override fun onComplete(values: ArrayList<String>) {
                        itemsRecommended = ArrayList(allItems.filter {
                            itemHasPreferredWords(it) ||
                                    itemHasPreferredOwner(it, values) ||
                                    itemHasPreferredCity(it) ||
                                    itemHasPreferredCategory(it) ||
                                    itemHasPreferredExchangeOptions(it)
                        })

                        itemsRecommended.shuffle()
                        _items.value = itemsRecommended
                    }

                    override fun onError(e: Exception?) {
                        // ignore the owners preferences
                        itemsRecommended = ArrayList(allItems.filter {
                            itemHasPreferredWords(it) ||
                                    itemHasPreferredCity(it) ||
                                    itemHasPreferredCategory(it) ||
                                    itemHasPreferredExchangeOptions(it)
                        })

                        itemsRecommended.shuffle()
                        _items.value = itemsRecommended
                    }
                })
        } else {
            // ignore the owners preferences
            itemsRecommended = ArrayList(allItems.filter {
                itemHasPreferredWords(it) ||
                        itemHasPreferredCity(it) ||
                        itemHasPreferredCategory(it) ||
                        itemHasPreferredExchangeOptions(it)
            })

            itemsRecommended.shuffle()
            _items.value = itemsRecommended
        }
    }

    private fun itemHasPreferredCategory(item: Item): Boolean {
        return currentUser.notifications.preferredCategories.any { category -> item.category == category }
    }

    private fun itemHasPreferredExchangeOptions(item: Item): Boolean {
        return if (item is ItemExchange) {
            item.exchangePreferences.intersect(currentUser.notifications.preferredExchangePreferences.toSet())
                .isNotEmpty()
        } else
            false
    }

    private fun itemHasPreferredCity(item: Item): Boolean {
        return currentUser.notifications.preferredCities.any { city ->
            item.city.equals(city, true)
        }
    }

    private fun itemHasPreferredOwner(item: Item, ownersEmails: ArrayList<String>): Boolean {
        return ownersEmails.any { email ->
            email == item.owner
        }
    }

    private fun itemHasPreferredWords(item: Item): Boolean {
        return currentUser.notifications.preferredWords.any { text ->
            val words = text.split(" ")
            // for any text provided as preference from current user, verify if all words
            // are contained in either item's title or item's description
            val containedInTitle = words.all { word ->
                item.name.contains(word, true)
            }
            val containedInDescription = words.all { word ->
                item.description.contains(word, true)
            }
            containedInDescription || containedInTitle
        }
    }
}