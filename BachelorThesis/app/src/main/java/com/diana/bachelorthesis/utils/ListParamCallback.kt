package com.diana.bachelorthesis.utils

interface ListParamCallback <T>{
    fun onComplete(values: ArrayList<T>)
    fun onError(e: Exception?)
}