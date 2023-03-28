package com.diana.bachelorthesis.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

open class Item(
    @DocumentId
    var itemId: String,
    var category: ItemCategory,
    var city: String = "-",
    var condition: ItemCondition?,
    var description: String,
    var location: GeoPoint,
    var name: String,
    var photos: ArrayList<String>,
    var owner: String,
    var postDate: Timestamp,
    var year: Int?
)