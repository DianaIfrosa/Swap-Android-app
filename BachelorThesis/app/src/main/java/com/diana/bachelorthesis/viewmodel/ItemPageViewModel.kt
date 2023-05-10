package com.diana.bachelorthesis.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.repository.PhotoRepository
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.NoParamCallback
import java.lang.Exception

class ItemPageViewModel: ViewModel() {
    private val TAG: String = ItemPageViewModel::class.java.name
    lateinit var currentItem: Item
    private val itemRepository = ItemRepository.getInstance()
    private val photoRepository = PhotoRepository.getInstance()

    fun deleteItem(callback: NoParamCallback) {
        Log.d(TAG, "Deleting item from owner  ${currentItem.owner} and id ${currentItem.itemId}")

        itemRepository.deleteItem(currentItem.itemId, (currentItem is ItemExchange), object: NoParamCallback {
            override fun onComplete() {
                photoRepository.deleteItemPhotos(currentItem.owner, currentItem.itemId)
                callback.onComplete()
            }

            override fun onError(e: Exception?) {
                callback.onError(e)
            }
        })
    }
}