package com.diana.bachelorthesis.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.*
import com.diana.bachelorthesis.repository.*
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback
import java.util.*
import kotlin.collections.ArrayList

class ItemPageViewModel : ViewModel() {
    private val TAG: String = ItemPageViewModel::class.java.name
    lateinit var currentItem: Item

    private val itemRepository = ItemRepository.getInstance()
    private val photoRepository = PhotoRepository.getInstance()
    private val proposalRepository = ProposalRepository.getInstance()
    private val chatRepository = ChatRepository.getInstance()
    private val userRepository = UserRepository.getInstance()

    var proposal: Proposal? = null
    lateinit var currentUser: User
    private val _currentOwner = MutableLiveData<User?>()
    val currentOwner: LiveData<User?> = _currentOwner

    fun deleteItem(callback: NoParamCallback) {
        Log.d(TAG, "Deleting item from owner  ${currentItem.owner} and id ${currentItem.itemId}")

        itemRepository.deleteItem(
            currentItem.itemId,
            (currentItem is ItemExchange),
            object : NoParamCallback {
                override fun onComplete() {
                    photoRepository.deleteItemPhotos(currentItem.owner, currentItem.itemId)
                    callback.onComplete()
                }

                override fun onError(e: Exception?) {
                    callback.onError(e)
                }
            })
    }

    fun createDonationProposalAndRetrieveChat(callback: OneParamCallback<Chat>) {
        // Verify if proposal already exists in db
        proposalRepository.getProposals(object : ListParamCallback<Proposal> {
            override fun onComplete(values: ArrayList<Proposal>) {
                values.forEach { p ->
                    if (p.itemId1 == currentItem.itemId || p.itemId2 == currentItem.itemId) {
                        proposal = p
                        return@forEach
                    }
                }

                if (proposal == null) {
                    // first time proposal
                    proposal = Proposal(
                        proposalId = UUID.randomUUID().toString(),
                        confirmation1 = false,
                        confirmation2 = true,
                        itemId1 = currentItem.itemId,
                        itemId2 = null,
                        userId1 = currentItem.owner,
                        userId2 = currentUser.email
                    )

                    proposalRepository.createProposal(proposal!!, object : NoParamCallback {
                        override fun onComplete() {
                            retrieveChat(callback)
                        }

                        override fun onError(e: Exception?) {
                            callback.onError(e)
                        }
                    })
                } else {
                    // proposal already existed
                    retrieveChat(callback)
                }
            }

            override fun onError(e: Exception?) {
                callback.onError(e)
            }
        })
    }

    private fun chatExists(otherUserEmail: String): String? {
        // null if it does not exists and a string if it exists (being the id)
        currentUser.chatIds.forEach {
            if (it["chatId"] == (currentUser.email + " " + otherUserEmail))
                return (currentUser.email + " " + otherUserEmail)
            else if (it["chatId"] == (otherUserEmail + " " + currentUser.email))
                return (otherUserEmail + " " + currentUser.email)
        }
        return null
    }

    fun retrieveChat(callback: OneParamCallback<Chat>) {
        val chatId = chatExists(currentItem.owner)
        if (chatId != null) {
            // get the chat
            chatRepository.getChats(
                0,
                arrayListOf(Pair(chatId, "false")),
                arrayListOf(),
                object :
                    ListParamCallback<Chat> {
                    override fun onComplete(values: ArrayList<Chat>) {
                        if (values.size == 1) {
                            val newChat = values[0]
                            newChat.otherUser = currentOwner.value
                            callback.onComplete(values[0])
                        }
                    }

                    override fun onError(e: Exception?) {
                        callback.onError(e)
                    }
                })
        } else {
            // create a new chat
            val newChat = Chat(
                id = currentUser.email + " " + currentItem.owner,
                otherUser = currentOwner.value
            )
            callback.onComplete(newChat)
        }
    }

    fun getUserData() {
        userRepository.getUserData(currentItem.owner, object: OneParamCallback<User> {
            override fun onComplete(value: User?) {
                   _currentOwner.value = value
            }

            override fun onError(e: java.lang.Exception?) {
                _currentOwner.value = null
            }
        })
    }
}