package com.diana.bachelorthesis.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ItemCondition(
    val displayName: String
) : Parcelable {
    NEW("New/Like new"),
    USED("Used"),
    NOT_WORKING("For parts or not working"),
    UNKNOWN("Unknown");

    companion object {
        fun stringToItemCondition(name: String): ItemCondition {
            for (condition in values()) if (condition.displayName.equals(
                    name,
                    true
                )
            ) return condition

            return UNKNOWN
        }
    }
}