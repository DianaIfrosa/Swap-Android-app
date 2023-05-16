package com.diana.bachelorthesis.model

data class Chat(
    val id: String,
    val user: User,
    var listMessages: List<Message>
)
