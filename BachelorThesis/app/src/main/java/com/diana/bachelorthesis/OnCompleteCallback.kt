package com.diana.bachelorthesis

interface ListParamCallback <T>{
    fun onComplete(values: ArrayList<T>)
    //TODO maybe add onException fun
}