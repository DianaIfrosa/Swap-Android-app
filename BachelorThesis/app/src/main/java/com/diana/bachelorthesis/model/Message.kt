package com.diana.bachelorthesis.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    val date: Timestamp = Timestamp(0, 0),
    val photoUri: String? = null,
    val proposalId: String? = null,
    val senderEmail: String = "",
    val text: String? = null
    ): Parcelable


