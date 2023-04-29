package com.diana.bachelorthesis.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import java.time.LocalDate


data class History(
    @DocumentId
    var operationId: String,
    var confirmation1: Boolean = false,
    var confirmation2: Boolean = false,
    val date: LocalDate,
    var feedback1: String? = null,
    var feedback2: String? = null,
    val item1: Item,
    val item2: Item? = null,
    val donationReceiver: User? = null // used only when the history object refers to a donation
)