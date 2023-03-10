package com.diana.bachelorthesis.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import java.time.Year

class ItemExchange(
    userEmail: String = "",
    category: ItemCategory = ItemCategory.UNKNOWN,
    description: String = "",
    var exchangePreferences: ArrayList<ItemCategory> = arrayListOf(),
    var exchangeInfo: History? = null, // null => is available
    location: GeoPoint = GeoPoint(0.0, 0.0),
    name: String = "",
    photos: ArrayList<String> = arrayListOf(),
    postDate: Timestamp = Timestamp(0,0),
    year: Int? = null
): Item (userEmail, category, description, location, name, photos, postDate, year) {
}