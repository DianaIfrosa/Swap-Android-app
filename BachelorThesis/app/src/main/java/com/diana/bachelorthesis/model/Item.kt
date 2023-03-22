package com.diana.bachelorthesis.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

open class Item(
    @DocumentId
    val itemId: String,
    var category: ItemCategory,
    var city: String = "-",
    var description: String,
    var location: GeoPoint,
    var name: String,
    var photos: ArrayList<String>,
    val owner: String,
    val postDate: Timestamp,
    var year: Int?
)