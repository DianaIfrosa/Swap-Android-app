package com.diana.bachelorthesis.adapters

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.LayoutParams
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.CardMessageBinding
import com.diana.bachelorthesis.model.*
import com.diana.bachelorthesis.repository.ItemRepository
import com.diana.bachelorthesis.repository.ProposalRepository
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.OneParamCallback
import com.diana.bachelorthesis.utils.ProposalCardClickListener
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MessagesRecyclerViewAdapter(
    private var currentUser: User, private var messages: List<Message>, var context: Context,
    private val onMessageClicked: (Item, Item?, Proposal) -> Unit
) :
    RecyclerView.Adapter<MessagesRecyclerViewAdapter.MessageViewHolder>(),
    ProposalCardClickListener {

    private val TAG: String = MessagesRecyclerViewAdapter::class.java.name
    private val itemRepository: ItemRepository = ItemRepository.getInstance()
    private val proposalRepository: ProposalRepository = ProposalRepository.getInstance()
    private val userRepository: UserRepository = UserRepository.getInstance()

    inner class MessageViewHolder(val cardMessageBinding: CardMessageBinding) :
        RecyclerView.ViewHolder(cardMessageBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding: CardMessageBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.card_message,
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    fun getPhotoLayoutParams(currentMessage: Message): LayoutParams {
        var params: LayoutParams
        if (currentMessage.senderEmail == currentUser.email) {
                params = LayoutParams(
                    context.resources.getDimension(R.dimen.photo_chat_size).toInt(),
                    context.resources.getDimension(R.dimen.photo_chat_size).toInt(),
                ).apply {
                    gravity = Gravity.END
                }
        } else {
            params = LayoutParams(
                context.resources.getDimension(R.dimen.photo_chat_size).toInt(),
                context.resources.getDimension(R.dimen.photo_chat_size).toInt(),
            ).apply {
                gravity = Gravity.START
            }
        }
        return params
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentMessage = messages[position]
        var itemProposal1: Item
        var itemProposal2: Item?
        var forExchange: Boolean
        var proposal: Proposal

        holder.cardMessageBinding.message = currentMessage

        // set position of message
        val params: LayoutParams
        if (currentMessage.senderEmail == currentUser.email) {
                params =LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.END
                }
        } else {
            params = LayoutParams(
               LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.START
            }
        }

        holder.cardMessageBinding.date.layoutParams = params

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.getDefault())
        holder.cardMessageBinding.date.text = dateFormatter.format(
            currentMessage.date.toDate()
        ).toString()

        if (currentMessage.text != null) {
            holder.cardMessageBinding.messageText.text = currentMessage.text
            holder.cardMessageBinding.messagePicture.visibility = View.GONE
            holder.cardMessageBinding.proposalCardLayout.visibility = View.GONE
            holder.cardMessageBinding.messageText.visibility = View.VISIBLE
            holder.cardMessageBinding.messageText.layoutParams = params

        } else if (currentMessage.photoUri != null) {
            holder.cardMessageBinding.messagePicture.layoutParams = getPhotoLayoutParams(currentMessage)
            holder.cardMessageBinding.messageText.visibility = View.GONE
            holder.cardMessageBinding.proposalCardLayout.visibility = View.GONE
            holder.cardMessageBinding.messagePicture.visibility = View.VISIBLE

            Glide.with(context).load(currentMessage.photoUri)
                .into(holder.cardMessageBinding.messagePicture)
        } else if (currentMessage.proposalId != null) {
            holder.cardMessageBinding.proposalCardLayout.layoutParams = params
            holder.cardMessageBinding.messageText.visibility = View.GONE
            holder.cardMessageBinding.messagePicture.visibility = View.GONE
            holder.cardMessageBinding.proposalCardLayout.visibility = View.VISIBLE
            proposalRepository.getProposalByID(
                currentMessage.proposalId,
                object : OneParamCallback<Proposal> {
                    override fun onComplete(value: Proposal?) {
                        Log.d(TAG, "Retrieved proposal.")
                        // get the items
                        if (value != null) {
                            proposal = value

                            holder.cardMessageBinding.proposalCard.proposal = proposal

                            forExchange = value.itemId2 != null
                            Log.d(TAG, "Proposal is forExchange $forExchange")

                            if (forExchange) {
                                holder.cardMessageBinding.proposalCard.layoutCard.setBackgroundResource(R.color.purple_pale)
                                itemRepository.getExchangeItem(
                                    proposal.itemId1,
                                    object : OneParamCallback<Item> {
                                        override fun onComplete(value: Item?) {
                                            Log.d(
                                                TAG,
                                                "Retrieved first item from exchange proposal."
                                            )
                                            if (value != null) {
                                                itemProposal1 = value

                                                itemRepository.getExchangeItem(
                                                    proposal.itemId2!!,
                                                    object : OneParamCallback<Item> {
                                                        override fun onComplete(value: Item?) {
                                                            Log.d(
                                                                TAG,
                                                                "Retrieved second item from exchange proposal."
                                                            )
                                                            if (value != null) {
                                                                itemProposal2 = value
                                                                updateUIForExchangeProposal(holder, itemProposal1, itemProposal2!!)
                                                            }
                                                        }

                                                        override fun onError(e: Exception?) {}

                                                    })
                                            }

                                        }

                                        override fun onError(e: Exception?) {}

                                    })
                            } else { //for donation
                                holder.cardMessageBinding.proposalCard.layoutCard.setBackgroundResource(R.color.yellow_pale)
                                itemRepository.getDonationItem(proposal.itemId1, object: OneParamCallback<Item> {
                                    override fun onComplete(value: Item?) {
                                        Log.d(
                                            TAG,
                                            "Retrieved item from donation proposal."
                                        )
                                        if (value != null) {
                                            itemProposal1 = value
                                            userRepository.getUserData(proposal.userId2, object: OneParamCallback<User> {
                                                override fun onComplete(value: User?) {
                                                    if (value != null) {
                                                        updateUIForDonationProposal(
                                                            holder,
                                                            itemProposal1,
                                                            value
                                                        )
                                                    }
                                                }

                                                override fun onError(e: Exception?) {}

                                            })
                                        }
                                    }

                                    override fun onError(e: Exception?) {}
                                })
                            }
                        }
                    }

                    override fun onError(e: Exception?) {}
                })
        }
    }

    private fun updateUIForExchangeProposal(holder: MessagesRecyclerViewAdapter.MessageViewHolder, itemProposal1: Item, itemProposal2: Item) {

        holder.cardMessageBinding.proposalCard.item2Layout.visibility = View.VISIBLE
        holder.cardMessageBinding.proposalCard.donationReceiverLayout.visibility = View.GONE

        holder.cardMessageBinding.proposalCard.item1 = itemProposal1
        holder.cardMessageBinding.proposalCard.item2 = itemProposal2
        holder.cardMessageBinding.proposalCard.proposalClickListener = this

        Glide.with(context).load(itemProposal1.photos[0]).into(holder.cardMessageBinding.proposalCard.item1Photo)
        Glide.with(context).load(itemProposal2.photos[0]).into(holder.cardMessageBinding.proposalCard.item2Photo)

        holder.cardMessageBinding.proposalCard.symbolImage.setImageResource(R.drawable.ic_arrows_exchange)
    }

    private fun updateUIForDonationProposal(holder: MessagesRecyclerViewAdapter.MessageViewHolder, itemProposal1: Item, donationReceiver: User) {

        holder.cardMessageBinding.proposalCard.item2Layout.visibility = View.GONE
        holder.cardMessageBinding.proposalCard.donationReceiverLayout.visibility = View.VISIBLE

        holder.cardMessageBinding.proposalCard.item1 = itemProposal1
        holder.cardMessageBinding.proposalCard.item2 = null
        holder.cardMessageBinding.proposalCard.proposalClickListener = this
        holder.cardMessageBinding.proposalCard.donationReceiverName.text = donationReceiver.name

        Glide.with(context).load(itemProposal1.photos[0]).into(holder.cardMessageBinding.proposalCard.item1Photo)
        Glide.with(context).load(donationReceiver.profilePhoto).into(holder.cardMessageBinding.proposalCard.donationReceiverPicture)

        holder.cardMessageBinding.proposalCard.symbolImage.setImageResource(R.drawable.ic_arrow_donation)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun cardClicked(item1: Item, item2: Item?, proposal: Proposal) {
      // todo call view func to navigate to proposal page
        Log.d(TAG, "Clicked on proposal item with id ${proposal.proposalId}")
        onMessageClicked(item1, item2, proposal)
    }

}