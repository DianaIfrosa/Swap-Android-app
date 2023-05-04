package com.diana.bachelorthesis.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.repository.UserRepository

class ItemPageViewModel: ViewModel() {
    private val TAG: String = ItemPageViewModel::class.java.name
    lateinit var currentItem: Item
}