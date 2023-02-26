package com.diana.bachelorthesis.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import java.time.Year

class ItemDonation(
    userEmail: String,
    available: Boolean,
    categories: ArrayList<ItemCategory>,
    description: String,
    var donationInfo: History?,
    location: GeoPoint,
    name: String,
    photos: ArrayList<String>,
    postDate: Timestamp,
    year: Year?
) : Item(userEmail, available, categories, description, location, name, photos, postDate, year) {
}