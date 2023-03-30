package com.diana.bachelorthesis

interface OneParamCallback<T> {
    fun onComplete(url: T?)
}