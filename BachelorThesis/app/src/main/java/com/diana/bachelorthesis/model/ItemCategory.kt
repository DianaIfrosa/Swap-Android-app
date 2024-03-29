package com.diana.bachelorthesis.model

import android.content.Context
import android.os.Parcelable
import com.diana.bachelorthesis.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ItemCategory : Parcelable {
    ACCESSORIES,
    APPLIANCES,
    BEAUTY,
    CLOTHESSHOES,
    DEVICES,
    EDUCATION,
    FOODDRINK,
    FURNITURE,
    GAMES,
    GARDEN,
    HOMEDECOR,
    JEWELRY,
    MEDICAL,
    UNKNOWN;

    companion object {
        fun stringToItemCategory(context: Context, name: String): ItemCategory {
            return when (name) {
                context.getString(R.string.categ_accessories) -> ACCESSORIES
                context.getString(R.string.categ_beauty) -> BEAUTY
                context.getString(R.string.categ_appliances) -> APPLIANCES
                context.getString(R.string.categ_clothesshoes) -> CLOTHESSHOES
                context.getString(R.string.categ_devices) -> DEVICES
                context.getString(R.string.categ_education) -> EDUCATION
                context.getString(R.string.categ_fooddrink) -> FOODDRINK
                context.getString(R.string.categ_furniture) -> FURNITURE
                context.getString(R.string.categ_garden) -> GARDEN
                context.getString(R.string.categ_home_decor) -> HOMEDECOR
                context.getString(R.string.categ_jewelry) -> JEWELRY
                context.getString(R.string.categ_games) -> GAMES
                context.getString(R.string.categ_medical) -> MEDICAL
                else -> UNKNOWN
            }
        }

        fun getTranslatedName(context: Context, item: ItemCategory): String {
            return when (item) {
                ACCESSORIES -> context.getString(R.string.categ_accessories)
                APPLIANCES -> context.getString(R.string.categ_appliances)
                BEAUTY ->  context.getString(R.string.categ_beauty)
                CLOTHESSHOES -> context.getString(R.string.categ_clothesshoes)
                DEVICES -> context.getString(R.string.categ_devices)
                EDUCATION -> context.getString(R.string.categ_education)
                FOODDRINK -> context.getString(R.string.categ_fooddrink)
                FURNITURE -> context.getString(R.string.categ_furniture)
                GARDEN -> context.getString(R.string.categ_garden)
                HOMEDECOR -> context.getString(R.string.categ_home_decor)
                JEWELRY -> context.getString(R.string.categ_jewelry)
                GAMES -> context.getString(R.string.categ_games)
                MEDICAL -> context.getString(R.string.categ_medical)
                UNKNOWN -> context.getString(R.string.unknown)
            }
        }
    }

}