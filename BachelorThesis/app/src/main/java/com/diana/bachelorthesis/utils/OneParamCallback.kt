package com.diana.bachelorthesis.utils

interface OneParamCallback<T> {
    fun onComplete(value: T?)
    fun onError(e: java.lang.Exception?)
}