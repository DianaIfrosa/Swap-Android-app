package com.diana.bachelorthesis.utils

import android.net.Uri
import com.diana.bachelorthesis.model.ItemCategory

interface ProfileOptionsListener {

    fun saveNotificationOption(option: Int)
    fun saveProfileChanges(photoUri: Uri?, newPass: String?)
    fun savePreferencesForRecommendations(
        words: List<String>,
        owners: List<String>,
        cities: List<String>,
        categories: List<ItemCategory>,
        exchangePreferences: List<ItemCategory>
    )
}