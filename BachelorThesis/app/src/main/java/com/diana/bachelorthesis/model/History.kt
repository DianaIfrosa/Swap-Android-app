package com.diana.bachelorthesis.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId


data class History(
    @DocumentId
    var eventId: String = "",
    val date: Timestamp = Timestamp(0,0),
//    var feedback1: String? = null,
//    var feedback2: String? = null,
    var item1: String = "",
    var item2: String? = null,
    val donationReceiverEmail: String? = null // used only when the history object refers to a donation
) : Parcelable {
    constructor(parcel: Parcel) : this(
        eventId = parcel.readString() ?: "",
        date =  parcel.readParcelable(Timestamp::class.java.classLoader) ?: Timestamp(0,0),
//        feedback1 = parcel.readString(),
//        feedback2 = parcel.readString(),
        item1 = parcel.readString() ?: "",
        item2 = parcel.readString(),
        donationReceiverEmail = parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        writeToParcel(parcel, flags)
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