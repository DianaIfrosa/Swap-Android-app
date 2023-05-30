package com.diana.bachelorthesis.repository

import android.util.Log
import com.diana.bachelorthesis.model.Chat
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Singleton

@Singleton
class ChatRepository {
    private val TAG: String = ChatRepository::class.java.name
    val db = Firebase.firestore

    val COLLECTION_NAME = "Chats"

    var chatsListenerRegistration: ListenerRegistration? = null
    var chatListenerRegistration: ListenerRegistration? = null

    companion object {
        @Volatile
        private var instance: ChatRepository? = null
        fun getInstance() = instance ?: ChatRepository().also { instance = it }
    }

    fun getChatsAndListen(chatsIds: List<Pair<String, String>>, callback: ListParamCallback<Chat>) {
        chatsListenerRegistration =
            db.collection(COLLECTION_NAME).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for chats", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    Log.d(TAG, "Started to retrieve chats")
                    getChats(0, chatsIds, arrayListOf(), callback)
                } else {
                    Log.w(TAG, "No such snapshot for chats")
                }
            }
    }

    fun getChats(
        currentPosition: Int,
        chatsIds: List<Pair<String, String>>,
        result: ArrayList<Chat>,
        callback: ListParamCallback<Chat>
    ) {
        val currentChat = chatsIds[currentPosition]

        db.collection(COLLECTION_NAME).document(currentChat.first).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chat = task.result.toObject(Chat::class.java)
                    if (chat != null) {
                        chat.seen = currentChat.second.toBoolean()

                        result.add(chat)
                        if (currentPosition == chatsIds.size - 1) {
                            callback.onComplete(result)
                        } else {
                            getChats(currentPosition + 1, chatsIds, result, callback)
                        }
                    } else {
                        Log.w(TAG, "Error retrieving chats. Not existing chats or null chat.")
                        callback.onError(task.exception)
                    }
                } else {
                    Log.w(TAG, "Error retrieving chats")
                    callback.onError(task.exception)
                }
            }
    }

    fun listenToChatChanges(chatId: String, callback: OneParamCallback<Chat>) {
        chatListenerRegistration =
            db.collection(COLLECTION_NAME).document(chatId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for current chat with id $chatId", error)
                    callback.onError(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val chatRetrieved = snapshot.toObject(Chat::class.java)
                    callback.onComplete(chatRetrieved)

                } else {
                    Log.w(TAG, "No such snapshot for chat with id $chatId")
                }
            }
    }

    fun addNewMessageToChat(chat: Chat, callback: NoParamCallback) {
        db.collection(COLLECTION_NAME).document(chat.id).update(
            "messages", FieldValue.arrayUnion(chat.messages.last())
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.w(TAG, "Successfully added new message to chat with id ${chat.id}")
                callback.onComplete()
            } else {
                Log.w(TAG, "Error occurred while adding new message to chat with id ${chat.id}")
                callback.onError(task.exception)
            }
        }
    }

    fun createNewChatAndAddMessage(chat: Chat, callback: NoParamCallback) {
        val messages = hashMapOf(
            "messages" to chat.messages
        )

        db.collection(COLLECTION_NAME).document(chat.id).set(messages)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.w(
                        TAG,
                        "Successfully created new chat and added first message to chat with id ${chat.id}"
                    )
                    callback.onComplete()
                } else {
                    Log.w(
                        TAG,
                        "Successfully created new chat and added first message to chat with id ${chat.id}"
                    )
                    callback.onError(task.exception)
                }
            }
    }


    fun detachChatListeners() {
        chatsListenerRegistration?.remove()
    }

    fun detachCurrentChatListeners() {
        chatListenerRegistration?.remove()
    }
}