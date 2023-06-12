package com.diana.bachelorthesis.utils

import com.diana.bachelorthesis.model.History
import com.diana.bachelorthesis.model.Item

interface HistoryCardClickListener {
    fun cardClicked(item1: Item, item2: Item?, history: History)
}