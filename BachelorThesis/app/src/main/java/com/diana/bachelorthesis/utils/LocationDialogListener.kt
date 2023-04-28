package com.diana.bachelorthesis.utils

import com.google.android.libraries.places.api.model.Place


interface LocationDialogListener {

    fun saveLocation(location: Place?)
}