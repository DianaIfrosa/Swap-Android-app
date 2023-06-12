package com.diana.bachelorthesis.utils

interface CustomClickListener<T> {
    fun cardClicked(value: T?)
    fun closeCardClicked()
}