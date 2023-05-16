package com.diana.bachelorthesis.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Message(
    @DocumentId
    val id: String = "",
    val date: Timestamp = Timestamp(0, 0),
    val photoUri: String? = null,
    val proposalId: String? = null,
    val senderEmail: String = "",
    val text: String? = null
    )
