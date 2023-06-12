package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.ViewModel

class FavoritesViewModel : ViewModel() {
    private val TAG: String = FavoritesViewModel::class.java.name

    var lastScrollPosition = 0
}