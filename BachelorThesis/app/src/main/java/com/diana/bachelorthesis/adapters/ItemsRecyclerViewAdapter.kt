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
import com.diana.bachelorthesis.model.Item
import com.diana.bachelorthesis.repository.PhotoRepository

class ItemsRecyclerViewAdapter(private var itemsList: List<Item>, var context: Context) :
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
        // TODO open item page -> another fragment
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
            val url = PhotoRepository.getInstance().unavailablePhotoUrl
            photosList.add(SlideModel(url, scaleType = ScaleTypes.CENTER_CROP))
        }

        return photosList
    }

}