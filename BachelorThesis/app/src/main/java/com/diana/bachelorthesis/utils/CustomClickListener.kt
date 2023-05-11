package com.diana.bachelorthesis.utils

import com.diana.bachelorthesis.model.Item

interface CustomClickListener {
    fun cardClicked(item: Item?)
    fun closeCardClicked()
}