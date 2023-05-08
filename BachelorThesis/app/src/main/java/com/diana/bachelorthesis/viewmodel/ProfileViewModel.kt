package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.utils.ListParamCallback

class ProfileViewModel: ViewModel() {
    private val TAG: String = ProfileViewModel::class.java.name
    private val itemRepository = ItemRepository.getInstance()

    fun getAllCities(callback: ListParamCallback<String>) {
        var retrievedCitiesExchanges = false
        var retrievedCitiesDonations = false

        val cities = mutableSetOf<String>()
        itemRepository.getExchangeItemsCities(object: ListParamCallback<String> {
            override fun onComplete(values: ArrayList<String>) {
                values.forEach {
                    cities.add(it)
                }
                retrievedCitiesExchanges = true
                if (retrievedCitiesDonations) {
                    callback.onComplete(ArrayList(cities))
                }
            }

            override fun onError(e: Exception?) {
                retrievedCitiesExchanges = true
                if (retrievedCitiesDonations) {
                    callback.onComplete(ArrayList(cities))
                }
            }

        })

        itemRepository.getDonationsItemsCities(object: ListParamCallback<String> {
            override fun onComplete(values: ArrayList<String>) {
                values.forEach {
                    cities.add(it)
                }
                retrievedCitiesDonations = true
                if (retrievedCitiesExchanges) {
                    callback.onComplete(ArrayList(cities))
                }
            }

            override fun onError(e: Exception?) {
                retrievedCitiesDonations = true
                if (retrievedCitiesExchanges) {
                    callback.onComplete(ArrayList(cities))
                }
            }
        })
    }

}