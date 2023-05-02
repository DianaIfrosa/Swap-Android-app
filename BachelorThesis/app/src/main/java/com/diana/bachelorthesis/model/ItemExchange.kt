package com.diana.bachelorthesis.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import kotlinx.parcelize.Parcelize

class ItemExchange(
    itemId: String = "",
    category: ItemCategory = ItemCategory.UNKNOWN,
    city: String = "-",
    condition: ItemCondition = ItemCondition.UNKNOWN,
    description: String = "",
    var exchangePreferences: ArrayList<ItemCategory> = arrayListOf(),
    var exchangeInfo: History? = null, // null => is available
    location: GeoPoint = GeoPoint(0.0, 0.0),
    name: String = "",
    owner: String = "",
    photos: ArrayList<String> = arrayListOf(),
    postDate: Timestamp = Timestamp(0,0),
    year: Int? = null
): Item (itemId, category, city, condition, description, location, name, photos, owner, postDate, year), Parcelable {
    constructor(parcel: Parcel) : this(
        itemId = parcel.readString() ?: "",
        category = parcel.readParcelable(ItemCategory::class.java.classLoader) ?: ItemCategory.UNKNOWN,
        city = parcel.readString() ?: "",
        condition = parcel.readParcelable(ItemCondition::class.java.classLoader) ?: ItemCondition.UNKNOWN,
        description = parcel.readString() ?: "",
        parcel.readArrayList(ItemCategory::class.java.classLoader) as ArrayList<ItemCategory>,
        parcel.readParcelable(History::class.java.classLoader),
        location = GeoPoint(parcel.readDouble(), parcel.readDouble()),
        name = parcel.readString() ?: "",
        owner = parcel.readString() ?: "",
        photos = parcel.createStringArrayList() as ArrayList<String>,
        postDate = parcel.readParcelable(Timestamp::class.java.classLoader) ?: Timestamp(0,0),
        year = parcel.readValue(Int::class.java.classLoader) as? Int
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeParcelable(exchangeInfo, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemExchange> {
        override fun createFromParcel(parcel: Parcel): ItemExchange {
            return ItemExchange(parcel)
        }

        override fun newArray(size: Int): Array<ItemExchange?> {
            return arrayOfNulls(size)
        }
    }

}