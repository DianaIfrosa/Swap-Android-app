package com.diana.bachelorthesis.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.diana.bachelorthesis.utils.CustomClickListener
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.CardItemBinding
import com.diana.bachelorthesis.databinding.CardItemMinimalBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.PhotoRepository
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.OneParamCallback
import com.squareup.picasso.Picasso
import java.lang.Exception

class ItemsHorizontalRecyclerViewAdapter(private var itemsList: List<Item>, var context: Context,
                               private val onItemClicked: (Item) -> Unit) :
    RecyclerView.Adapter<ItemsHorizontalRecyclerViewAdapter.ItemViewHolder>(),
    CustomClickListener {

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
        holder.cardItemBinding.model = currentItem
        holder.cardItemBinding.itemClickListener = this
        Picasso.get().load(currentItem.photos[0]).into(holder.cardItemBinding.itemPhoto)
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    override fun cardClicked(item: Item?) {
        if (item != null) {
            onItemClicked(item)
        }
    }
}