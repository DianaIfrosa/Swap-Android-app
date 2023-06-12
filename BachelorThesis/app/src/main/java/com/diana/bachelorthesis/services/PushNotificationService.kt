package com.diana.bachelorthesis.services

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class PushNotificationService: FirebaseMessagingService() {
    private val TAG: String = PushNotificationService::class.java.name

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        // save token to shared preferences
        val sharedPref = this.getSharedPreferences("token", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("token", token)
            Log.d(TAG, "Saved token to SharedPreferences")
            commit() // synchronously
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "onMessageReceived is called")

        val dataMap: Map<String, String> = message.data
        val title = dataMap["title"]
        val body = dataMap["body"]
        val itemId = dataMap["itemId"]
        val itemForExchange = dataMap["forExchange"].toBoolean()

        val intent = Intent("push_notification")
        intent.putExtra("title", title)
        intent.putExtra("body", body)
        intent.putExtra("itemId", itemId)
        intent.putExtra("itemForExchange", itemForExchange)
        sendBroadcast(intent)
    }

}