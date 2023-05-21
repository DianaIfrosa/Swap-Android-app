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

class OwnerProfileViewModel : ViewModel() {

    lateinit var owner: User
    private val itemRepository = ItemRepository.getInstance()
    private val _allItems = MutableLiveData<ArrayList<Item>?>()
    var allItems: LiveData<ArrayList<Item>?> = _allItems

    fun getItemsFromOwner() {
        val items = arrayListOf<Item>()
        // get exchange items
        itemRepository.getItemsFromOwner(owner.email, true, object: ListParamCallback<Item> {
            override fun onComplete(values: ArrayList<Item>) {
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
                valuesFiltered.forEach {
                   items.add(it)
               }
                // get donation items
                itemRepository.getItemsFromOwner(owner.email, false, object: ListParamCallback<Item> {
                    override fun onComplete(values: ArrayList<Item>) {
                        val valuesFiltered2 = ArrayList(values.filter { item ->
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
                        valuesFiltered2.forEach {
                            items.add(it)
                        }
                        items.shuffle()
                        _allItems.value = items
                    }

                    override fun onError(e: Exception?) {
                        items.shuffle()
                        _allItems.value = items
                    }
                })
            }

            override fun onError(e: Exception?) {
                // get donation items
                itemRepository.getItemsFromOwner(owner.email, false, object: ListParamCallback<Item> {
                    override fun onComplete(values: ArrayList<Item>) {
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
                        valuesFiltered.forEach {
                            items.add(it)
                        }
                        items.shuffle()
                        _allItems.value = items
                    }

                    override fun onError(e: Exception?) {
                        items.shuffle()
                        _allItems.value = null
                    }
                })
            }
        })
    }

}