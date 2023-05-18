package com.diana.bachelorthesis.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.Chat
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.ChatRepository
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback

class ChatViewModel : ViewModel() {
    private val TAG: String = ChatViewModel::class.java.name

    private val chatRepository = ChatRepository.getInstance()
    private val userRepository = UserRepository.getInstance()

    private val _chats = MutableLiveData<ArrayList<Chat>>()
    val chats: LiveData<ArrayList<Chat>> = _chats
    lateinit var currentUser: User

    fun listenToUserChatChanges(callback: OneParamCallback<User>) {
        userRepository.listenToCurrentUserChanges(
            currentUser.email,
            object : OneParamCallback<User> {
                override fun onComplete(value: User?) {
                    if (value != null && value.chatIds.size != currentUser.chatIds.size) {
                        // some other user started a conversation for the first time with the current user
                        currentUser = value
                        getUserChats()
                        callback.onComplete(currentUser)
                    }
                }

                override fun onError(e: java.lang.Exception?) {}
            })
    }

    fun getUserChats() {
        val pairsList = arrayListOf<Pair<String, String>>()
        Log.d(TAG, "Get user chats")
        currentUser.chatIds.forEach {
            pairsList.add(Pair(it["chatId"]!!, it["seen"]!!))
        }
        if (pairsList.isNotEmpty()) {

            chatRepository.getChatsAndListen(pairsList, object : ListParamCallback<Chat> {
                override fun onComplete(values: ArrayList<Chat>) {
                    // here the chats are retrieved at every change in db
                    Log.d(TAG, "Retrieved all chats!")

                    if (chats.value == null || (chats.value != null && chatsDiffer(chats.value!!, values))) {
                        // first time data fetch or modification in db
                        val chatsRetrieved = values
                        val otherUsersEmails = getOtherUsersEmails(chatsRetrieved)
                        userRepository.getUsersByEmail(
                            otherUsersEmails,
                            0,
                            arrayListOf(),
                            object : ListParamCallback<User?> {
                                override fun onComplete(values: ArrayList<User?>) {
                                    Log.d(TAG, "Retrieved the users corresponding to the chats!")
                                    val chatsWithUsers = arrayListOf<Chat>()
                                    chatsRetrieved.forEachIndexed { index, chat ->
                                        if (values[index] != null) { // if other user is null then skip the chat
                                            chatsWithUsers.add(
                                                Chat(
                                                    chat.id,
                                                    values[index],
                                                    chat.messages,
                                                    chat.seen
                                                )
                                            )
                                        }
                                    }
                                    Log.d(TAG, "Chats modification!")
                                    _chats.value = sortChatsByDate(chatsWithUsers)
                                }

                                override fun onError(e: Exception?) {
                                    // TODO ?
                                }

                            })
                    } else {
                        Log.d(TAG, "Chat modification in DB didn't affect the current user.")
                    }
                }

                override fun onError(e: Exception?) {
                    // TODO ?
                }

            })
        } else {
            _chats.value = arrayListOf()
        }
    }

    private fun chatsDiffer(list1: ArrayList<Chat>, list2: ArrayList<Chat>): Boolean {
        val sortedList1 = sortChatsByDate(list1)
        val sortedList2 = sortChatsByDate(list2)
        if (sortedList1.size != sortedList2.size)
            return true

        sortedList1.forEachIndexed { index, chat ->
            if (chat.messages.size != sortedList2[index].messages.size)
                return true
        }
        return false
    }

    private fun getOtherUsersEmails(userChatsSorted: ArrayList<Chat>): List<String> {
        val result = arrayListOf<String>()
        userChatsSorted.forEach { chat ->
            val emailsFromId = chat.id.split(" ")
            if (emailsFromId[0] != currentUser.email) {
                result.add(emailsFromId[0])
            } else {
                result.add(emailsFromId[1])
            }
        }
        return result
    }

    private fun sortChatsByDate(userChatsSorted: ArrayList<Chat>): ArrayList<Chat> {
        // sort messages from each chat from new to old
        val result = userChatsSorted
        result.forEach { chat ->
            val messagesSorted = chat.messages.sortedByDescending { message ->
                message.date
            }

            chat.messages = ArrayList(messagesSorted)
        }

        // sort chats based on last message (newest first)
        return ArrayList(result.sortedByDescending { chat ->
            chat.messages[0].date
        })
    }

    fun detachListeners() {
        chatRepository.detachChatListeners()
        userRepository.detachCurrentUserListener()
    }
}