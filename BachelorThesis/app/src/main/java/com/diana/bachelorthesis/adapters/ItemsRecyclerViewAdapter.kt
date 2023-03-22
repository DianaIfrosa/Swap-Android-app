package com.diana.bachelorthesis.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.diana.bachelorthesis.CustomClickListener
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.CardItemBinding
import com.diana.bachelorthesis.model.Item

class ItemsRecyclerViewAdapter(var itemsList: List<Item>, var context: Context) :
    RecyclerView.Adapter<ItemsRecyclerViewAdapter.ItemViewHolder>(),
    CustomClickListener {

    private val TAG: String = ItemsRecyclerViewAdapter::class.java.name

    inner class ItemViewHolder(val cardItemBinding: CardItemBinding) :
        RecyclerView.ViewHolder(cardItemBinding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: CardItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.card_item,
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        Log.d(TAG, "on Bind for position $position")
        val currentItem: Item = itemsList[position]
        holder.cardItemBinding.model = currentItem
        holder.cardItemBinding.itemClickListener = this
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    override fun cardClicked(item: Item?) {
        // TODO open item page -> another activity
        Toast.makeText(
            context, "You clicked " + item!!.name,
            Toast.LENGTH_LONG
        ).show()
    }

}