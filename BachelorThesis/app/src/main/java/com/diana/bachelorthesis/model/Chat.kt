package com.diana.bachelorthesis.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class Chat(
    @DocumentId
    val id: String = "",
    val otherUser: User? = null,
    var messages: Map<String, Message> = mapOf(),
    var messagesSorted: ArrayList<Message> = arrayListOf(),
    var seen: Boolean = false
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chat

        if (messages.size != other.messages.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (otherUser?.hashCode() ?: 0)
        result = 31 * result + messages.hashCode()
        result = 31 * result + seen.hashCode()
        return result
    }
}
