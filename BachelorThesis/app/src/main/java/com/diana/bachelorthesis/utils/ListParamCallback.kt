package com.diana.bachelorthesis.utils

interface ListParamCallback <T>{
    fun onComplete(values: ArrayList<T>)
    //TODO maybe add onException fun
}