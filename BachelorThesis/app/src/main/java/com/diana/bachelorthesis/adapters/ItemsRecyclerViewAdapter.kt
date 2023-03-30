package com.diana.bachelorthesis.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.diana.bachelorthesis.CustomClickListener
import com.diana.bachelorthesis.R
import com.diana.bachelorthesis.databinding.CardItemBinding
import com.diana.bachelorthesis.model.Item

class ItemsRecyclerViewAdapter(private var itemsList: List<Item>, var context: Context) :
    RecyclerView.Adapter<ItemsRecyclerViewAdapter.ItemViewHolder>(),
    CustomClickListener {

    private val TAG: String = ItemsRecyclerViewAdapter::class.java.name
    private val unavailablePhotoUrl = "https://firebasestorage.googleapis.com/v0/b/bachelorthesis-3092d.appspot.com/o/unavailable.jpg?alt=media&token=bbf854a6-162d-47e4-9006-7d8fe5ef083e"

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
        holder.cardItemBinding.model = currentItem
        holder.cardItemBinding.itemClickListener = this
        holder.cardItemBinding.photoCarousel.setImageList(getPhotos(currentItem))
        holder.cardItemBinding.photoCarousel
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

    private fun getPhotos(item: Item): List<SlideModel> {
        val photosList = ArrayList<SlideModel>()
        for (photo in item.photos) {
            photosList.add(SlideModel(photo, scaleType = ScaleTypes.CENTER_CROP))
        }

        if (photosList.isEmpty()) {
            photosList.add(SlideModel(unavailablePhotoUrl, scaleType = ScaleTypes.CENTER_CROP))
        }

        return photosList
    }

}