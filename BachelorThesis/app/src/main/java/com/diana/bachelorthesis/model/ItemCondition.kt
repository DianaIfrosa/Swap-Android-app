package com.diana.bachelorthesis.model

import android.content.Context
import android.os.Parcelable
import com.diana.bachelorthesis.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ItemCondition: Parcelable {
    NEW,
    USED,
    NOT_WORKING,
    UNKNOWN;

    companion object {
        fun stringToItemCondition(context: Context, name: String): ItemCondition {
            return when (name) {
                context.getString(R.string.new_like_new) -> NEW
                context.getString(R.string.used) -> USED
                context.getString(R.string.not_working) -> NOT_WORKING
                else -> UNKNOWN
            }
        }

        fun getTranslatedName(context: Context, item: ItemCondition): String {
            return when (item) {
                NEW -> context.getString(R.string.new_like_new)
                USED -> context.getString(R.string.used)
                NOT_WORKING -> context.getString(R.string.not_working)
                else -> context.getString(R.string.unknown)
            }
        }
    }
}