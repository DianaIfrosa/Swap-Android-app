package com.diana.bachelorthesis.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationsPreferences(
    var notificationsOption: Int = 2,
    var preferredOwners: List<String> = listOf(),
    var preferredCities: List<String> = listOf(),
    var preferredWords: List<String> = listOf(),
    var preferredCategories: List<ItemCategory> = listOf(),
    var preferredExchangePreferences: List<ItemCategory> = listOf()
): Parcelable
{
    fun clone() = NotificationsPreferences(
        notificationsOption,
        ArrayList(preferredOwners.map{it}),
        ArrayList(preferredCities.map{it}),
        ArrayList(preferredWords.map{it}),
        preferredCategories.map {it},
        preferredExchangePreferences.map {it},
    )
}