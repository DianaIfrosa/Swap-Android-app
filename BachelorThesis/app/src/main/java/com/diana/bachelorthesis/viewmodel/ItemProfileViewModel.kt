package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.utils.OneParamCallback
import java.lang.Exception

class ItemProfileViewModel: ViewModel() {
    private val TAG: String = ItemProfileViewModel::class.java.name
    private val itemRepository = ItemRepository.getInstance()
    lateinit var item: Item
    private var isFavorite: Boolean? = null

    fun isFavorite(): Boolean =  isFavorite ?: false

    fun setIsFavorite() {
        itemRepository.checkIsFavorite(item.itemId, object: OneParamCallback<Boolean> {
            override fun onComplete(value: Boolean?) {
                isFavorite = value ?: false
            }

            override fun onError(e: Exception?) {
                isFavorite = false
            }
        })
    }
}