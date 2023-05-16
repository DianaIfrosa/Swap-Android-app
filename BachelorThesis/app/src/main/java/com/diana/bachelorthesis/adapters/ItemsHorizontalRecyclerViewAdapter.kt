package com.diana.bachelorthesis.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.diana.bachelorthesis.utils.CustomClickListener
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.CardItemMinimalBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemExchange
import com.squareup.picasso.Picasso

class ItemsHorizontalRecyclerViewAdapter(
    private var itemsList: List<Item>,
    var canCloseCard: Boolean,
    var context: Context,
    private val onItemClosed: (() -> Unit)?,
    private val onItemClicked: (Item) -> Unit
) :
    RecyclerView.Adapter<ItemsHorizontalRecyclerViewAdapter.ItemViewHolder>(),
    CustomClickListener<Item> {

    private val TAG: String = ItemsHorizontalRecyclerViewAdapter::class.java.name

    inner class ItemViewHolder(val cardItemBinding: CardItemMinimalBinding) :
        RecyclerView.ViewHolder(cardItemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: CardItemMinimalBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.card_item_minimal,
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem: Item = itemsList[position]
        holder.cardItemBinding.cardLayout.setBackgroundResource(R.drawable.btn_transparent_with_stroke)

        if (canCloseCard) {
            holder.cardItemBinding.btnClose.visibility = View.VISIBLE
        } else {
            holder.cardItemBinding.btnClose.visibility = View.GONE
        }

        if (currentItem is ItemExchange) {
            holder.cardItemBinding.cardLayout.setBackgroundResource(R.drawable.background_exchange_item)
        } else {
            holder.cardItemBinding.cardLayout.setBackgroundResource(R.drawable.background_donation_item)
        }

        holder.cardItemBinding.model = currentItem
        holder.cardItemBinding.itemClickListener = this
        Picasso.get().load(currentItem.photos[0]).into(holder.cardItemBinding.itemPhoto)
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    override fun cardClicked(value: Item?) {
        if (value != null) {
            onItemClicked(value)
        }
    }

    override fun closeCardClicked() {
        onItemClosed?.let { it() }
    }
}