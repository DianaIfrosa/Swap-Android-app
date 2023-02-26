package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddItemViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply{
        value = "This is add item fragment"
    }
    val text: LiveData<String> = _text
}