package com.diana.bachelorthesis.adapters

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.CardMessageBinding
import com.diana.bachelorthesis.model.Message
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.utils.CustomClickListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class MessagesRecyclerViewAdapter(private var currentUser: User, private var messages: List<Message>, var context: Context,
                                  private val onMessageClicked: (Message) -> Unit) :
RecyclerView.Adapter<MessagesRecyclerViewAdapter.MessageViewHolder>(),
    CustomClickListener<Message> {

    private val TAG: String = MessagesRecyclerViewAdapter::class.java.name

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

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentMessage = messages[position]

        holder.cardMessageBinding.message = currentMessage
        holder.cardMessageBinding.messageClickListener = this

        // set position of message

        val params: LinearLayout.LayoutParams
        if (currentMessage.senderEmail == currentUser.email) {
            params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END
            }
        } else {
             params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.START
            }
        }

        holder.cardMessageBinding.messageText.layoutParams = params
        holder.cardMessageBinding.date.layoutParams = params
        holder.cardMessageBinding.messagePicture.layoutParams = params

        if (currentMessage.text != null) {
            // todo add for attachment as well
            holder.cardMessageBinding.messagePicture.visibility = View.GONE
            holder.cardMessageBinding.messageText.visibility = View.VISIBLE
            holder.cardMessageBinding.messageText.text = currentMessage.text
        } else if (currentMessage.photoUri != null) {
            holder.cardMessageBinding.messageText.visibility = View.GONE
            holder.cardMessageBinding.messagePicture.visibility = View.VISIBLE
            Picasso.get().load(currentMessage.photoUri).into(holder.cardMessageBinding.messagePicture)
        }

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.getDefault())
        holder.cardMessageBinding.date.text =  dateFormatter.format(
            currentMessage.date.toDate()
        ).toString()

    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun cardClicked(value: Message?) {
        if (value?.proposalId != null) {
            onMessageClicked(value)
        }
    }

    override fun closeCardClicked() {}

}