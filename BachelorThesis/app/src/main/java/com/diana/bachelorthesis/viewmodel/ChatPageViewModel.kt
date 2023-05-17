package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Chat
import com.diana.bachelorthesis.model.Message
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.ChatRepository
import com.diana.bachelorthesis.utils.OneParamCallback
import java.lang.Exception

class ChatPageViewModel : ViewModel() {
    private val TAG: String = ChatPageViewModel::class.java.name

    private val chatRepository = ChatRepository.getInstance()
    lateinit var currentUser: User

    private val _currentChat = MutableLiveData<Chat>()
    val currentChat: LiveData<Chat> = _currentChat

    fun listenToChatChanges() {
        chatRepository.listenToChatChanges(currentChat.value!!.id, object : OneParamCallback<Chat> {
            override fun onComplete(value: Chat?) {
                if (value != null) {
                    if (value.messages.size != currentChat.value!!.messages.size) {
                        val newChat = Chat(
                            id = currentChat.value!!.id,
                            otherUser = currentChat.value!!.otherUser,
                            messages = value.messages,
                            seen = currentChat.value!!.seen
                        )
                        val messagesSorted = sortMessages(value)
                        newChat.messagesSorted = messagesSorted
                        _currentChat.value = newChat
                    }
                }
            }

            override fun onError(e: Exception?) {}

        })
    }

    fun setCurrentChat(chat: Chat) {
        chat.messagesSorted = sortMessages(chat)
        _currentChat.value = chat
    }

    private fun sortMessages(newChat: Chat): ArrayList<Message> {
        val messagesSorted = newChat.messages.values.sortedBy { message ->
            message.date
        }
        return ArrayList(messagesSorted)
    }

    fun detachCurrentChatListeners() {
        chatRepository.detachCurrentChatListeners()
    }

}