package com.diana.bachelorthesis.model

data class Mail(
    var to: String,
    var message: Map<String, String>
)