package com.diana.bachelorthesis.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class Chat(
    @DocumentId
    val id: String = "",
    var otherUser: User? = null,
    var messages: ArrayList<Message> = arrayListOf(),
    var seen: Boolean = false
) : Parcelable {
    fun clone() = this.copy(otherUser = if (this.otherUser != null) this.otherUser!!.clone() else null, messages = ArrayList(this.messages.map { it.copy() }))
}
