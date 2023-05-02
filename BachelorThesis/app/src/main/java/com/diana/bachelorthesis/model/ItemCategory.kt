package com.diana.bachelorthesis.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ItemCategory(
    val displayName: String, // TODO modify this as it is not useful for translation to Romanian or other languages
    private var availableItems: ArrayList<Item> = ArrayList(),
    private var totalItems: Int = 0
) : Parcelable {
    APPLIANCES("Appliances"),
    CLOTHESSHOES("Clothes & Shoes"),
    DEVICES("Devices"),
    EDUCATION("Education"),
    FOODDRINK("Food & Drink"),
    FURNITURE("Furniture"),
    GAMES("Games"),
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

    companion object {
        fun stringToItemCategory(name: String): ItemCategory {
            for (categ in values())
                if (categ.displayName.equals(name, true))
                    return categ

            return UNKNOWN
        }
    }
}