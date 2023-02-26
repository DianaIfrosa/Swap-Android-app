package com.diana.bachelorthesis.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference

class User(
    @DocumentId
    val email: String,
    var name: String,
    var favoritePosts: ArrayList<Item> = ArrayList(),
    var preferences: ArrayList<String> = ArrayList(),
    var profilePhoto: String? = null
) {
}