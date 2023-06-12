package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.*


class HistoryViewModel : ViewModel() {
    private val TAG: String = HistoryViewModel::class.java.name
    var lastScrollPosition = 0
    lateinit var currentUser: User

}