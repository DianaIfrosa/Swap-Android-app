package com.diana.bachelorthesis.viewmodel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Chat
import com.diana.bachelorthesis.model.Message
import com.diana.bachelorthesis.model.Proposal
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.ChatRepository
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback
import com.google.firebase.Timestamp
import java.lang.Exception

class ChatPageViewModel : ViewModel() {
    private val TAG: String = ChatPageViewModel::class.java.name

    private val chatRepository = ChatRepository.getInstance()
    private val userRepository = UserRepository.getInstance()
    lateinit var currentUser: User

    private val _currentChat = MutableLiveData<Chat>()
    val currentChat: LiveData<Chat> = _currentChat

    var proposal: Proposal? = null

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
                        newChat.messages = messagesSorted
                        _currentChat.value = newChat
                    }
                }
            }

            override fun onError(e: Exception?) {}

        })
    }

    fun sendProposalIfExisting() {
        if (proposal != null) {
            addMessageToChat(proposalId = proposal!!.proposalId)
        }
    }

    fun setCurrentChat(chat: Chat) {
        chat.messages = sortMessages(chat)
        _currentChat.value = chat
    }

    private fun sortMessages(newChat: Chat): ArrayList<Message> {
        val messagesSorted = newChat.messages.sortedBy { message ->
            message.date
        }
        return ArrayList(messagesSorted)
    }

    fun detachCurrentChatListeners() {
        chatRepository.detachCurrentChatListeners()
    }

    fun addMessageToChat(textMessage: String? = null, proposalId: String? = null) {
        val newChat: Chat = currentChat.value!!
        val newMessage = Message(date = Timestamp.now(), senderEmail = currentUser.email, text = textMessage, proposalId = proposalId)
        newChat.messages.add(newMessage)

        _currentChat.value = newChat

        if (currentChat.value!!.messages.size == 1) {
            // first time sending a message
            chatRepository.createNewChatAndAddMessage(currentChat.value!!, object: NoParamCallback {
                override fun onComplete() {
                    userRepository.addChatIdToUserList(currentChat.value!!.id, currentUser.email)
                    userRepository.addChatIdToUserList(currentChat.value!!.id, currentChat.value!!.otherUser!!.email)
                }

                override fun onError(e: Exception?) {}

            })
        } else {
            chatRepository.addNewMessageToChat(currentChat.value!!, object : NoParamCallback {
                override fun onComplete() {}
                override fun onError(e: Exception?) {}
            })
        }
    }

}