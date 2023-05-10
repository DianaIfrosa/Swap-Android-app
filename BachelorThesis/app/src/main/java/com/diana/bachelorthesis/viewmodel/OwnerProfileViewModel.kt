package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.NoParamCallback

class OwnerProfileViewModel : ViewModel() {

    lateinit var owner: User
    private val itemRepository = ItemRepository.getInstance()
    var allItems: ArrayList<Item> = arrayListOf()

    fun getItemsFromOwner(callback: NoParamCallback) {
        val items = arrayListOf<Item>()
        // get exchange items
        itemRepository.getItemsFromOwner(owner.email, true, object: ListParamCallback<Item> {
            override fun onComplete(values: ArrayList<Item>) {
               values.forEach {
                   items.add(it)
               }
                // get donation items
                itemRepository.getItemsFromOwner(owner.email, false, object: ListParamCallback<Item> {
                    override fun onComplete(values: ArrayList<Item>) {
                        values.forEach {
                            items.add(it)
                        }
                        items.shuffle()
                        allItems = items
                        callback.onComplete()
                    }

                    override fun onError(e: Exception?) {
                        items.shuffle()
                        allItems = items
                        callback.onComplete()
                    }
                })
            }

            override fun onError(e: Exception?) {
                // get donation items
                itemRepository.getItemsFromOwner(owner.email, false, object: ListParamCallback<Item> {
                    override fun onComplete(values: ArrayList<Item>) {
                        values.forEach {
                            items.add(it)
                        }
                        items.shuffle()
                        allItems = items
                        callback.onComplete()
                    }

                    override fun onError(e: Exception?) {
                        items.shuffle()
                        allItems = items
                        callback.onComplete()
                    }
                })
            }
        })
    }

}