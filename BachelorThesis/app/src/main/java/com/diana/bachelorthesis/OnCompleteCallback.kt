package com.diana.bachelorthesis

import com.diana.bachelorthesis.model.Item

interface OnCompleteCallback {
    fun onCompleteGetItems(items: ArrayList<Item>)
}