package com.diana.bachelorthesis.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.util.*


data class History(
    @DocumentId
    var eventId: String = "",
    var confirmation1: Boolean = false,
    var confirmation2: Boolean = false,
    val date: Date? = null,
    var feedback1: String? = null,
    var feedback2: String? = null,
    val item1: String = "",
    val item2: String? = null,
    val donationReceiverEmail: String? = null // used only when the history object refers to a donation
) : Parcelable {
    constructor(parcel: Parcel) : this(
        eventId = parcel.readString() ?: "",
        confirmation1 = parcel.readByte() != 0.toByte(),
        confirmation2 = parcel.readByte() != 0.toByte(),
        date = Date(parcel.readLong()),
        feedback1 = parcel.readString(),
        feedback2 = parcel.readString(),
        item1 = parcel.readString() ?: "",
        item2 = parcel.readString(),
        donationReceiverEmail = parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(eventId)
        parcel.writeByte(if (confirmation1) 1 else 0)
        parcel.writeByte(if (confirmation2) 1 else 0)
        parcel.writeLong(date!!.time)
        parcel.writeString(feedback1)
        parcel.writeString(feedback2)
        parcel.writeString(item1)
        parcel.writeString(item2)
        parcel.writeString(donationReceiverEmail)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<History> {
        override fun createFromParcel(parcel: Parcel): History {
            return History(parcel)
        }

        override fun newArray(size: Int): Array<History?> {
            return arrayOfNulls(size)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is History) return false
        return (this.confirmation1 == other.confirmation1 && this.confirmation2 == other.confirmation2)
    }
}