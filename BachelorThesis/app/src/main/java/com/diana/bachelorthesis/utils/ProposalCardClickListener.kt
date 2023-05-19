package com.diana.bachelorthesis.utils

import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.Proposal

interface ProposalCardClickListener {
    fun cardClicked(item1: Item, item2: Item?, proposal: Proposal)
}