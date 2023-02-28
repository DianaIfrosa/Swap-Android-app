package com.diana.bachelorthesis.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import java.time.Year

class ItemExchange(
    userEmail: String,
    category: ItemCategory,
    description: String,
    var exchangePreferences: ArrayList<ItemCategory>,
    var exchangeInfo: History?, // null => is available
    location: GeoPoint,
    name: String,
    photos: ArrayList<String>,
    postDate: Timestamp,
    year: Year?
): Item (userEmail, category, description, location, name, photos, postDate, year) {
}