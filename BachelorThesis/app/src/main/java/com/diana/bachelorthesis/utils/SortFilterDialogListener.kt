package com.diana.bachelorthesis.utils

import com.diana.bachelorthesis.model.ItemCategory

interface SortFilterDialogListener {

    fun saveSortOption(option: Int)
    fun saveFilterOptions(city: String, categories: List<ItemCategory>)
}