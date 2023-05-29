package com.diana.bachelorthesis.viewmodel

import androidx.lifecycle.ViewModel
import com.diana.bachelorthesis.model.*
import com.diana.bachelorthesis.repository.*
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback
import com.google.firebase.Timestamp
import java.lang.Exception
import java.util.*

class ProposalPageViewModel : ViewModel() {
    private val TAG: String = ProposalPageViewModel::class.java.name

    private val userRepository = UserRepository.getInstance()
    private val itemRepository = ItemRepository.getInstance()
    private val proposalRepository = ProposalRepository.getInstance()
    private val historyRepository = HistoryRepository.getInstance()
    private val categoriesRepository = CategoriesRepository.getInstance()

    lateinit var userMakingProposal: User
    lateinit var currentUser: User
    lateinit var item1: Item
    var item2: Item? = null

    lateinit var proposal: Proposal

    fun getUserMakingProposalData(callback: NoParamCallback) {
        if (proposal.userId1 == currentUser.email) {
            // the current user is the one receiving the proposal
            userRepository.getUserData(proposal.userId2, object : OneParamCallback<User> {
                override fun onComplete(value: User?) {
                    if (value != null) {
                        userMakingProposal = value
                        callback.onComplete()
                    }
                }

                override fun onError(e: Exception?) {
                    callback.onError(e)
                }
            })
        } else {
            // the current user is making the proposal
            userMakingProposal = currentUser
            callback.onComplete()
        }

    }

    fun confirmItemsAreStillAvailable(callback: OneParamCallback<Boolean>) {
        if (item2 != null) {
            // exchange
            itemRepository.getExchangeItem(item1.itemId, object : OneParamCallback<Item> {
                override fun onComplete(value: Item?) {
                    if (value != null) {
                        value as ItemExchange
                        if (value.exchangeInfo != null) {
                            callback.onComplete(false)
                        }
                        itemRepository.getExchangeItem(
                            item2!!.itemId,
                            object : OneParamCallback<Item> {
                                override fun onComplete(value: Item?) {
                                    if (value != null) {
                                        value as ItemExchange
                                        if (value.exchangeInfo != null) {
                                            callback.onComplete(false)
                                        }
                                        callback.onComplete(true)
                                    }
                                }

                                override fun onError(e: Exception?) {
                                    callback.onError(e)
                                }
                            })

                    }
                }
                override fun onError(e: Exception?) {
                    callback.onError(e)
                }

            })
        } else {
            // donation
            itemRepository.getDonationItem(item1.itemId, object : OneParamCallback<Item> {
                override fun onComplete(value: Item?) {
                    if (value != null) {
                        value as ItemDonation
                        if (value.donationInfo != null) {
                            callback.onComplete(false)
                        }
                        callback.onComplete(true)
                    }
                }

                override fun onError(e: Exception?) {
                  callback.onError(e)
                }
            })
        }
    }

    fun confirmProposal(callback: NoParamCallback) {
        proposalRepository.confirmProposal(proposal.proposalId, object: NoParamCallback {
            override fun onComplete() {
                val id = UUID.randomUUID().toString()
                val history = History(
                    eventId = id,
                    date = Timestamp.now(),
                    item1 = item1.itemId,
                    item2 = item2?.itemId,
                    donationReceiverEmail = if (item2 == null) userMakingProposal.email else null
                )
                historyRepository.addHistory(history, object: NoParamCallback {
                    override fun onComplete() {
                       markItemsAsGiven(id, callback)
                    }

                    override fun onError(e: Exception?) {
                       callback.onError(e)
                    }

                })
            }

            override fun onError(e: Exception?) {
                callback.onError(e)
            }

        })
    }

    private fun markItemsAsGiven(historyId: String, callback: NoParamCallback) {

        if (item2 != null) {
            // exchange
            itemRepository.markExchangeItemAsGiven(historyId, item1.itemId, object : NoParamCallback {
                override fun onComplete() {
                    categoriesRepository.markItemAsGiven(item1.category.name)
                    itemRepository.markExchangeItemAsGiven(historyId, item2!!.itemId, object : NoParamCallback {
                        override fun onComplete() {
                            categoriesRepository.markItemAsGiven(item2!!.category.name)
                           callback.onComplete()
                        }

                        override fun onError(e: Exception?) {
                           callback.onError(e)
                        }
                    })
                }

                override fun onError(e: Exception?) {
                    callback.onError(e)
                }

            })

        } else {
            itemRepository.markDonationItemAsGiven(historyId, item1.itemId, object: NoParamCallback {
                override fun onComplete() {
                    categoriesRepository.markItemAsGiven(item1.category.name)
                   callback.onComplete()
                }

                override fun onError(e: Exception?) {
                  callback.onError(e)
                }

            })
        }
    }

}