package com.diana.bachelorthesis.model

import android.util.Log
import com.google.firebase.firestore.util.Logger.warn

data class Notifications(
    var allPosts: ArrayList<String> = ArrayList(),
    var prefferedPostsOnly: ArrayList<String> = ArrayList()
)