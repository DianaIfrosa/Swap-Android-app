package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.*
import com.diana.bachelorthesis.repository.ChatRepository
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.repository.ProposalRepository
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback
import java.util.*
import kotlin.collections.ArrayList

class ItemChoiceViewModel : ViewModel() {
    private val TAG: String = ItemChoiceViewModel::class.java.name
    private val itemRepository = ItemRepository.getInstance()
    private val proposalRepository = ProposalRepository.getInstance()
    private val chatRepository = ChatRepository.getInstance()

    lateinit var currentUser: User
    lateinit var otherUser: User

    lateinit var items: ArrayList<Item>
    lateinit var itemToPropose: Item
    lateinit var currentItem: Item
    var proposal: Proposal? = null

    fun getCurrentUserObjects(callback: ListParamCallback<Item>) {
        itemRepository.getItemsFromOwner(currentUser.email, true, object : ListParamCallback<Item> {
            override fun onComplete(values: ArrayList<Item>) {
                val result = ArrayList(values.filter {
                    it as ItemExchange
                    it.exchangeInfo == null
                })
                items = result
                callback.onComplete(result)
            }

            override fun onError(e: Exception?) {
                callback.onError(e)
            }

        })
    }

    fun createProposalAndRetrieveChat(callback: OneParamCallback<Chat>) {
        // Verify if proposal already exists in db
        proposalRepository.getProposals(object : ListParamCallback<Proposal> {
            override fun onComplete(values: ArrayList<Proposal>) {
                values.forEach { p ->
                    if ((p.itemId1 == currentItem.itemId && p.itemId2 == itemToPropose.itemId) ||
                        ((p.itemId2 == currentItem.itemId && p.itemId1 == itemToPropose.itemId))
                    ) {
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
                        itemId2 = itemToPropose.itemId,
                        userId1 = currentItem.owner,
                        userId2 = itemToPropose.owner
                    )
                    proposalRepository.createProposal(proposal!!, object : NoParamCallback {
                        override fun onComplete() {
                            retrieveChat(callback)
                        }

                        override fun onError(e: java.lang.Exception?) {
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

    private fun retrieveChat(callback: OneParamCallback<Chat>) {
        val chatId = chatExists(currentItem.owner)
        if (chatId != null) {
            // get the chat
            chatRepository.getChats(
                0,
                arrayListOf(Pair(chatId, "false")),
                arrayListOf(),
                object : ListParamCallback<Chat> {
                    override fun onComplete(values: ArrayList<Chat>) {
                        if (values.size == 1) {
                            val newChat = values[0]
                            newChat.otherUser = otherUser
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
                otherUser = otherUser
            )
            callback.onComplete(newChat)
        }
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

}