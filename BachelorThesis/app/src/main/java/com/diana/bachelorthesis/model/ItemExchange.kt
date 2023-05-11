package com.diana.bachelorthesis.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

class ItemExchange(
    itemId: String = "",
    address: String = "",
    category: ItemCategory = ItemCategory.UNKNOWN,
    city: String = "-",
    condition: ItemCondition = ItemCondition.UNKNOWN,
    description: String = "",
    var exchangePreferences: ArrayList<ItemCategory> = arrayListOf(),
    var exchangeInfo: String? = null, // null => is available
    location: GeoPoint = GeoPoint(0.0, 0.0),
    name: String = "",
    owner: String = "",
    photos: ArrayList<String> = arrayListOf(),
    postDate: Timestamp = Timestamp(0,0),
    year: Int? = null
): Item (itemId, address, category, city, condition, description, location, name, photos, owner, postDate, year), Parcelable {
    constructor(parcel: Parcel) : this(
        itemId = parcel.readString() ?: "",
        address = parcel.readString() ?: "",
        category = parcel.readParcelable(ItemCategory::class.java.classLoader) ?: ItemCategory.UNKNOWN,
        city = parcel.readString() ?: "",
        condition = parcel.readParcelable(ItemCondition::class.java.classLoader) ?: ItemCondition.UNKNOWN,
        description = parcel.readString() ?: "",
        parcel.readArrayList(ItemCategory::class.java.classLoader) as ArrayList<ItemCategory>,
        exchangeInfo = parcel.readString(),
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

    override fun equals(other: Any?): Boolean {
        if (other !is ItemExchange) return false
        return (this.itemId == other.itemId &&
                this.category == other.category &&
                this.address == other.address &&
                this.city == other.city &&
                this.condition == other.condition &&
                this.description == other.description &&
                this.location == other.location &&
                this.name == other.name &&
                this.photos == other.photos &&
                this.owner == other.owner &&
                this.postDate == other.postDate &&
                this.year == other.year &&
                this.exchangePreferences == other.exchangePreferences &&
                this.exchangeInfo == other.exchangeInfo
                )
    }

}