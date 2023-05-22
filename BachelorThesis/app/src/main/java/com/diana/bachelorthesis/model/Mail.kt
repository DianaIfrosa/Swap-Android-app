package com.diana.bachelorthesis.model

data class Mail(
    var to: String,
    var message: Map<String, String>
) {
    fun clone() = Mail(
        to,
        message.toMap()
    )
}