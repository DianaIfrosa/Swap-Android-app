package com.diana.bachelorthesis.utils

import com.google.firebase.firestore.GeoPoint

interface LocationDialogListener {

    fun saveLocation(location: GeoPoint?)
}