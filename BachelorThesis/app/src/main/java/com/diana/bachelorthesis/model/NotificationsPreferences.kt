package com.diana.bachelorthesis.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationsPreferences(
    var notificationsOption: Int = 1,
    var preferredOwners: List<String> = listOf(),
    var preferredCities: List<String> = listOf(),
    var preferredWords: List<String> = listOf(),
    var preferredCategories: List<ItemCategory> = listOf(),
    var preferredExchangePreferences: List<ItemCategory> = listOf()
): Parcelable