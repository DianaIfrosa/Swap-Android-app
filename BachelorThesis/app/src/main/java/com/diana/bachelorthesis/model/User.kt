package com.diana.bachelorthesis.model

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @DocumentId
    val email: String = "",
    var name: String = "",
    var favoritePosts: ArrayList<Item> = ArrayList(),
    var preferences: ArrayList<String> = ArrayList(),
    var profilePhoto: String? = null
) : Parcelable