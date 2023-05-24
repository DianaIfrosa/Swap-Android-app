package com.diana.bachelorthesis.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Feedback (
    @DocumentId
    var id: String = "",
    var senderEmail: String = "",
    var timestamp: Timestamp = Timestamp(0,0),
    var explanation: String = ""
)