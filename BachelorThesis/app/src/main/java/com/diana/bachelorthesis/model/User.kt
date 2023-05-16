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
    var favoriteDonations: ArrayList<String> = ArrayList(),
    var favoriteExchanges: ArrayList<String> = ArrayList(),
    var profilePhoto: String? = null,
    var notifications: NotificationsPreferences = NotificationsPreferences(),
    val chatIds: ArrayList<Map<String, Boolean>> = ArrayList()
) : Parcelable