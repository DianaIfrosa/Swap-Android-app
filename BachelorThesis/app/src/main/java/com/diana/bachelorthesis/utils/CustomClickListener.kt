package com.diana.bachelorthesis.utils

import com.diana.bachelorthesis.model.Item

interface CustomClickListener<T> {
    fun cardClicked(value: T?)
    fun closeCardClicked()
}