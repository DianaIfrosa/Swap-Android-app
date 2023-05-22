package com.diana.bachelorthesis.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.CardHistoryBinding
import com.diana.bachelorthesis.model.History
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.HistoryCardClickListener
import com.diana.bachelorthesis.utils.OneParamCallback
import com.squareup.picasso.Picasso
import java.lang.Exception

class CardsHistoryAdapter(
    private var currentUser: User?,
    private var historyObjects: List<History>,
    private var itemsList: List<Pair<Item, Item?>>,
    var context: Context,
    private val onItemClicked: (Item, Item?, History) -> Unit
) :
    RecyclerView.Adapter<CardsHistoryAdapter.CardViewHolder>(), HistoryCardClickListener {
    private val TAG: String = CardsHistoryAdapter::class.java.name
    private val userRepository = UserRepository.getInstance()

    inner class CardViewHolder(val cardHistoryBinding: CardHistoryBinding) :
        RecyclerView.ViewHolder(cardHistoryBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding: CardHistoryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.card_history,
            parent,
            false
        )
        return CardViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return historyObjects.size
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val history: History = historyObjects[position]

        val item1 = itemsList[position].first
        val item2 = itemsList[position].second

        if (item2 != null) {
            // exchange card
            holder.cardHistoryBinding.item2Layout.visibility = View.VISIBLE
            holder.cardHistoryBinding.donationReceiverLayout.visibility = View.GONE
            holder.cardHistoryBinding.layoutCard.setBackgroundResource(R.color.purple_pale)
            holder.cardHistoryBinding.symbolImage.setImageResource(R.drawable.ic_arrows_exchange)

            Picasso.get().load(item1.photos[0]).into(holder.cardHistoryBinding.item1Photo)
            Picasso.get().load(item2.photos[0]).into(holder.cardHistoryBinding.item2Photo)
        } else {
            // donation card
            holder.cardHistoryBinding.item2Layout.visibility = View.GONE
            holder.cardHistoryBinding.donationReceiverLayout.visibility = View.VISIBLE
            holder.cardHistoryBinding.layoutCard.setBackgroundResource(R.color.yellow_pale)
            holder.cardHistoryBinding.symbolImage.setImageResource(R.drawable.ic_arrow_donation)

            Picasso.get().load(item1.photos[0]).into(holder.cardHistoryBinding.item1Photo)
        }

        holder.cardHistoryBinding.item1 = item1
        holder.cardHistoryBinding.item2 = item2
        holder.cardHistoryBinding.history = history.copy()
        holder.cardHistoryBinding.historyClickListener = this@CardsHistoryAdapter

        // get donation receiver photo
        if (item2 == null && history.donationReceiverEmail != null) {
            userRepository.getUserData(
                history.donationReceiverEmail,
                object : OneParamCallback<User> {
                    override fun onComplete(value: User?) {
                        if (value != null) {
                            Picasso.get().load(value.profilePhoto)
                                .into(holder.cardHistoryBinding.donationReceiverPicture)
                            if (value.email == currentUser!!.email) {
                                holder.cardHistoryBinding.donationReceiverName.text =
                                    context.getString(R.string.you)
                            } else {
                                holder.cardHistoryBinding.donationReceiverName.text = value.name
                            }
                        }
                    }

                    override fun onError(e: Exception?) {}
                })
        }
    }

    override fun cardClicked(item1: Item, item2: Item?, history: History) {
        onItemClicked(item1, item2, history)
    }

}
