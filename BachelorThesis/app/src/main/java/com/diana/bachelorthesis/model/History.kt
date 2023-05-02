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
    var operationId: String,
    var confirmation1: Boolean = false,
    var confirmation2: Boolean = false,
    val date: Date?,
    var feedback1: String? = null,
    var feedback2: String? = null,
    val item1: Item?,
    val item2: Item? = null,
    val donationReceiver: User? = null // used only when the history object refers to a donation
) : Parcelable {
    constructor(parcel: Parcel) : this(
        operationId = parcel.readString() ?: "",
        confirmation1 = parcel.readByte() != 0.toByte(),
        confirmation2 = parcel.readByte() != 0.toByte(),
        date = Date(parcel.readLong()),
        feedback1 = parcel.readString(),
        feedback2 = parcel.readString(),
        item1 = parcel.readParcelable(Item::class.java.classLoader),
        item2 = parcel.readParcelable(Item::class.java.classLoader),
        donationReceiver = parcel.readParcelable(User::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(operationId)
        parcel.writeByte(if (confirmation1) 1 else 0)
        parcel.writeByte(if (confirmation2) 1 else 0)
        parcel.writeLong(date!!.time)
        parcel.writeString(feedback1)
        parcel.writeString(feedback2)
        parcel.writeParcelable(item1, flags)
        parcel.writeParcelable(item2, flags)
        parcel.writeParcelable(donationReceiver, flags)
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
}