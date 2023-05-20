package com.diana.bachelorthesis.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class PostReport(
    @DocumentId
    var id: String = "",
    var reporterEmail: String = "",
    var timestamp: Timestamp = Timestamp(0,0),
    var postId: String = "",
    var postForExchange: Boolean = true,
    var reason: String = ""
)