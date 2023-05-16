package com.diana.bachelorthesis.utils

import com.diana.bachelorthesis.model.History
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.User

interface HistoryCardClickListener {
    fun cardClicked(item1: Item, item2: Item?, history: History)
}