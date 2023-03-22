package com.diana.bachelorthesis.utils

import android.content.Context
import android.location.Geocoder
import com.diana.bachelorthesis.model.Item

class LocationHelper(var applicationContext : Context) {

    fun getItemsCities(items: List<Item>): List<Item> {
        val geocoder = Geocoder(applicationContext)
        val result: MutableList<Item> = arrayListOf()

        items.forEach { item ->
            val addresses = geocoder.getFromLocation(item.location.latitude, item.location.longitude, 3)
            var resultCity: String? = null

            if (!addresses.isNullOrEmpty()) {
                for (address in addresses)
                    if (address.locality != null) {
                        resultCity = address.locality
                        break
                    }
            }
            result.add(item.apply{ city = resultCity ?: "-"})
        }

        return result
    }
}