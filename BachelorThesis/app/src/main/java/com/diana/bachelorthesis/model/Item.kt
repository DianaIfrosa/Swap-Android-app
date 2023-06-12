package com.diana.bachelorthesis.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import kotlin.collections.ArrayList

open class Item(
    @DocumentId
    var itemId: String,
    var address: String,
    var category: ItemCategory,
    var city: String = "-",
    var condition: ItemCondition?,
    var description: String,
    var location: GeoPoint,
    var name: String,
    var photos: ArrayList<String>,
    var owner: String,
    var postDate: Timestamp?,
    var year: Int?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        itemId = parcel.readString() ?: "",
        address = parcel.readString() ?: "",
        category = parcel.readParcelable(ItemCategory::class.java.classLoader)
            ?: ItemCategory.UNKNOWN,
        city = parcel.readString() ?: "",
        condition = parcel.readParcelable(ItemCondition::class.java.classLoader),
        description = parcel.readString() ?: "",
        location = GeoPoint(parcel.readDouble(), parcel.readDouble()),
        name = parcel.readString() ?: "",
        photos = parcel.createStringArrayList() as ArrayList<String>,
        owner = parcel.readString() ?: "",
        postDate = parcel.readParcelable(Timestamp::class.java.classLoader),
        year = parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(itemId)
        parcel.writeString(address)
        parcel.writeParcelable(category, flags)
        parcel.writeString(city)
        parcel.writeParcelable(condition, flags)
        parcel.writeString(description)
        parcel.writeDouble(location.latitude)
        parcel.writeDouble(location.longitude)
        parcel.writeString(name)
        parcel.writeStringList(photos)
        parcel.writeString(owner)
        parcel.writeParcelable(postDate, flags)
        parcel.writeValue(year)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Item> {
        override fun createFromParcel(parcel: Parcel): Item {
            return Item(parcel)
        }

        override fun newArray(size: Int): Array<Item?> {
            return arrayOfNulls(size)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Item) return false

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
                this.year == other.year
                )
    }
}