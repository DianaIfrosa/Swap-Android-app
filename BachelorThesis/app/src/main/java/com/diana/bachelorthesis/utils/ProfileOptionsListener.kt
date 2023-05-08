package com.diana.bachelorthesis.utils

import com.diana.bachelorthesis.model.ItemCategory

interface ProfileOptionsListener {

    fun saveNotificationOption(option: Int)
    fun saveProfileChanges() //todo add uri for profile photo and new pass?
    fun savePreferencesForRecommendations(
        words: List<String>,
        owners: List<String>,
        cities: List<String>,
        categories: List<ItemCategory>,
        exchangePreferences: List<ItemCategory>
    )
}