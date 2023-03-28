package com.diana.bachelorthesis.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import java.time.Year

class ItemDonation(
    itemId: String = "",
    owner: String = "",
    category: ItemCategory = ItemCategory.UNKNOWN,
    city: String = "-",
    condition: ItemCondition = ItemCondition.UNKNOWN,
    description: String = "",
    var donationInfo: History? = null, // null => is available
    location: GeoPoint = GeoPoint(0.0, 0.0),
    name: String = "",
    photos: ArrayList<String> = arrayListOf(),
    postDate: Timestamp = Timestamp(0,0),
    year: Int? = null
) : Item(itemId, category, city, condition, description, location, name, photos, owner, postDate, year) {
}