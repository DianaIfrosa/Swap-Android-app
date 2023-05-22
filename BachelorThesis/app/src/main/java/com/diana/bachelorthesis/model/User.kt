package com.diana.bachelorthesis.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
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
    var chatIds: ArrayList<Map<String, String>> = arrayListOf()
) : Parcelable {
    fun clone() = User(
        email,
        name,
        ArrayList(favoriteDonations.map{it}),
        ArrayList(favoriteExchanges.map{it}),
        profilePhoto,
        notifications.clone(),
        ArrayList(chatIds.map{it})
    )
}