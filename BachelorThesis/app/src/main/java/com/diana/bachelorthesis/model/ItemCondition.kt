package com.diana.bachelorthesis.model

enum class ItemCondition(
    val displayName: String
) {
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