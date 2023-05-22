package com.diana.bachelorthesis.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentId

data class Proposal(
    @DocumentId
    val proposalId: String = "",
    val confirmation1: Boolean = false,
    val confirmation2: Boolean = false,
    val itemId1: String = "",
    val itemId2: String? = null,
    val userId1: String = "",
    val userId2: String = ""
// exchange = all fields completed, with the first user being the one receiving the proposal
// donation = the above rules except item2 is null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(proposalId)
        parcel.writeByte(if (confirmation1) 1 else 0)
        parcel.writeByte(if (confirmation2) 1 else 0)
        parcel.writeString(itemId1)
        parcel.writeString(itemId2)
        parcel.writeString(userId1)
        parcel.writeString(userId2)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Proposal> {
        override fun createFromParcel(parcel: Parcel): Proposal {
            return Proposal(parcel)
        }

        override fun newArray(size: Int): Array<Proposal?> {
            return arrayOfNulls(size)
        }
    }
}
