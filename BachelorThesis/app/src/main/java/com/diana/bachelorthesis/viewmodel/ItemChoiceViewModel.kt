package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.utils.ListParamCallback

class ItemChoiceViewModel: ViewModel() {
    private val TAG: String = ItemChoiceViewModel::class.java.name
    private val itemRepository = ItemRepository.getInstance()

    lateinit var currentUser: User
    lateinit var items: ArrayList<Item>

    fun getCurrentUserObjects(callback: ListParamCallback<Item>) {
        itemRepository.getItemsFromOwner(currentUser.email, true, object: ListParamCallback<Item> {
            override fun onComplete(values: ArrayList<Item>) {
                val result = ArrayList(values.filter {
                    it as ItemExchange
                    it.exchangeInfo == null
                })
                items = result
                callback.onComplete(result)
            }

            override fun onError(e: Exception?) {
              callback.onError(e)
            }

        })
    }

}