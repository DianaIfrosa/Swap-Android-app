package com.diana.bachelorthesis.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.CardItemProposalExchangeBinding
import com.diana.bachelorthesis.model.Item

class ItemChoiceAdapter(private var itemsList: List<Item>, var context: Context) :
    RecyclerView.Adapter<ItemChoiceAdapter.ItemViewHolder>() {

    private val TAG: String = ItemChoiceAdapter::class.java.name
    var selectedPosition = -1

    inner class ItemViewHolder(val cardItemBinding: CardItemProposalExchangeBinding) :
        RecyclerView.ViewHolder(cardItemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: CardItemProposalExchangeBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.card_item_proposal_exchange,
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem: Item = itemsList[position]

        holder.cardItemBinding.item = currentItem
        Glide.with(context).load(currentItem.photos[0]).centerCrop().into(holder.cardItemBinding.itemPicture)

        holder.cardItemBinding.cardView.isChecked = selectedPosition == position

        holder.cardItemBinding.cardView.setOnClickListener {
            when (selectedPosition) {
                position -> {
                    // unselect the selected item
                    holder.cardItemBinding.cardView.isChecked = false
                    selectedPosition = -1
                }
                -1 -> {
                    // select for the first time
                    holder.cardItemBinding.cardView.isChecked = true
                    selectedPosition = position
                }
                else -> {
                    // select a different item
                    val lastSelectedPosition = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(lastSelectedPosition)
                    holder.cardItemBinding.cardView.isChecked = true
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

}