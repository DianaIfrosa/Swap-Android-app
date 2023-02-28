package com.diana.bachelorthesis.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import java.time.Year

open class Item(
    @DocumentId
    val userEmail: String,
    var category: ItemCategory,
    var description: String,
    var location: GeoPoint,
    var name: String,
    var photos: ArrayList<String>,
    val postDate: Timestamp,
    var year: Year?
) {
}