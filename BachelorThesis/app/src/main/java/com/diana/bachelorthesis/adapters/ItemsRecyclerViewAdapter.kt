package com.diana.bachelorthesis.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.diana.bachelorthesis.utils.CustomClickListener
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.CardItemBinding
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.model.ItemExchange
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.repository.PhotoRepository
import com.diana.bachelorthesis.repository.UserRepository
import com.diana.bachelorthesis.utils.OneParamCallback
import com.squareup.picasso.Picasso
import java.lang.Exception

class ItemsRecyclerViewAdapter(private var itemsList: List<Item>, var context: Context,
private val onItemClicked: (Item) -> Unit) :
    RecyclerView.Adapter<ItemsRecyclerViewAdapter.ItemViewHolder>(),
    CustomClickListener<Item> {

    private val TAG: String = ItemsRecyclerViewAdapter::class.java.name
    private val userRepository = UserRepository.getInstance()

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
        val currentItem: Item = itemsList[position]

        if (currentItem is ItemExchange) {
            holder.cardItemBinding.layoutCard.setBackgroundResource(R.color.purple_pale)
        } else {
            holder.cardItemBinding.layoutCard.setBackgroundResource(R.color.yellow_pale)
        }

        userRepository.getUserData(currentItem.owner, object: OneParamCallback<User> {
            override fun onComplete(value: User?) {
                if (value != null) {
                    holder.cardItemBinding.ownerName.text = value.name
                    Picasso.get().load(value.profilePhoto).into(holder.cardItemBinding.ownerPicture)
                } else {
                    holder.cardItemBinding.ownerName.text = "-"
                }
            }

            override fun onError(e: Exception?) {
                holder.cardItemBinding.ownerName.text = "-"
            }

        })
        holder.cardItemBinding.model = currentItem
        holder.cardItemBinding.itemClickListener = this
        holder.cardItemBinding.photoCarousel.setImageList(getPhotos(currentItem))
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    override fun cardClicked(value: Item?) {
        if (value != null) {
            onItemClicked(value)
        }
    }

    override fun closeCardClicked() {}

    companion object {
        fun getPhotos(item: Item): List<SlideModel> {
            val photosList = ArrayList<SlideModel>()
            for (photo in item.photos) {
                photosList.add(SlideModel(photo, scaleType = ScaleTypes.CENTER_CROP))
            }

            if (photosList.isEmpty()) {
                val url = PhotoRepository.getInstance().unavailablePhotoUrl
                photosList.add(SlideModel(url, scaleType = ScaleTypes.CENTER_CROP))
            }

            return photosList
        }
    }

}