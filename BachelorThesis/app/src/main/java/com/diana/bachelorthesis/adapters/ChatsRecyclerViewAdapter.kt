package com.diana.bachelorthesis.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.CardChatBinding
import com.diana.bachelorthesis.model.Chat
import com.diana.bachelorthesis.model.Message
import com.diana.bachelorthesis.utils.CustomClickListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatsRecyclerViewAdapter(private val listChats: ArrayList<Chat>, private val context: Context,
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

        Picasso.get().load(currentChat.user.profilePhoto).into(holder.cardChatBinding.userPicture)

        val lastMessage = getLastMessage(currentChat.listMessages)
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.cardChatBinding.chatLastDate.text  = dateFormatter.format(
            lastMessage.date.toDate()
        ).toString()

        if (lastMessage.text != null) {
            holder.cardChatBinding.chatLastMessage.text = lastMessage.text
        } else if (lastMessage.proposalId != null) {
            holder.cardChatBinding.chatLastMessage.text = context.getString(R.string.sent_proposal)
        } else if (lastMessage.photoUri != null) {
            holder.cardChatBinding.chatLastMessage.text = context.getString(R.string.sent_photo)
        }
    }

    private fun getLastMessage(listMessages: List<Message>): Message {
        return listMessages.maxBy { it.date.toDate() }
    }

    override fun getItemCount(): Int {
       return listChats.size
    }

    override fun cardClicked(value: Chat?) {
        onChatClicked(value!!)
    }

    override fun closeCardClicked() {}

}