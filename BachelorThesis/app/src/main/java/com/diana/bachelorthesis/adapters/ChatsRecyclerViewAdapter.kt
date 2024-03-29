package com.diana.bachelorthesis.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.CardChatBinding
import com.diana.bachelorthesis.model.Chat
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.utils.CustomClickListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatsRecyclerViewAdapter(private val currentUser: User, private val listChats: ArrayList<Chat>, private val context: Context,
                               private val onChatClicked: (Chat) -> Unit) :
    RecyclerView.Adapter<ChatsRecyclerViewAdapter.ChatViewHolder>(),
    CustomClickListener<Chat> {

    private val TAG: String = ChatsRecyclerViewAdapter::class.java.name

    inner class ChatViewHolder(val cardChatBinding: CardChatBinding) :
        RecyclerView.ViewHolder(cardChatBinding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatsRecyclerViewAdapter.ChatViewHolder {
        val binding: CardChatBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.card_chat,
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatsRecyclerViewAdapter.ChatViewHolder, position: Int) {
       val currentChat = listChats[position]

        holder.cardChatBinding.chatClickListener = this
        holder.cardChatBinding.chat = currentChat

        Glide.with(context).load(currentChat.otherUser!!.profilePhoto).centerCrop().into(holder.cardChatBinding.userPicture)

        val lastMessage = currentChat.messages[0]
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.cardChatBinding.chatLastDate.text  = dateFormatter.format(
            lastMessage.date.toDate()
        ).toString()

        Log.d(TAG, "Date last message ${lastMessage.date.toDate()}")

        if (lastMessage.text != null) {
            holder.cardChatBinding.chatLastMessage.text = lastMessage.text
        } else if (lastMessage.proposalId != null) {
            if (lastMessage.senderEmail == currentUser.email) {
                 holder.cardChatBinding.chatLastMessage.text =
                     context.getString(R.string.you_sent_a_proposal)
            } else {
                holder.cardChatBinding.chatLastMessage.text =
                    context.getString(R.string.sent_proposal)
            }
        } else if (lastMessage.photoUri != null) {
            if (lastMessage.senderEmail == currentUser.email) {
                holder.cardChatBinding.chatLastMessage.text = context.getString(R.string.you_sent_photo)
            } else {
                holder.cardChatBinding.chatLastMessage.text = context.getString(R.string.sent_photo)
            }
        }
    }

    override fun getItemCount(): Int {
       return listChats.size
    }

    override fun cardClicked(value: Chat?) {
        onChatClicked(value!!)
    }

    override fun closeCardClicked() {}

}