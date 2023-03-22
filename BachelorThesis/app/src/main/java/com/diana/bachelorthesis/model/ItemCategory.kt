package com.diana.bachelorthesis.model

enum class ItemCategory(
    val displayName: String,
    private var availableItems: ArrayList<Item> = ArrayList(),
    private var totalItems: Int = 0
) {
    APPLIANCES("Appliances"),
    CLOTHESSHOES("Clothes & Shoes"),
    DEVICES("Devices"),
    EDUCATION("Education"),
    FOODDRINK("Food & Drink"),
    FURNITURE("Furniture"),
    GARDEN("Garden"),
    MEDICAL("Medical"),
    UNKNOWN("Unknown");

    fun addItemToCategory(item: Item) {
        availableItems.add(item)
        totalItems++
    }

    fun removeItemFromCategory(item:Item) {
        val index = availableItems.indexOfFirst {
            it.owner == item.owner
        }
        availableItems.removeAt(index)
        totalItems--
    }
}