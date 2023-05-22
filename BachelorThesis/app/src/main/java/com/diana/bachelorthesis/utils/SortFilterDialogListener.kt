package com.diana.bachelorthesis.utils

import android.content.Context
import com.diana.bachelorthesis.model.ItemCategory

interface SortFilterDialogListener {

    fun saveSortOption(option: Int)
    fun saveFilterOptions(context: Context, city: String, categories: List<ItemCategory>)
}