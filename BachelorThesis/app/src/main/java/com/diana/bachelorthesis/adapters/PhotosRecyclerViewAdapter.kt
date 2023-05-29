package com.diana.bachelorthesis.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.diana.bachelorthesis.databinding.CardPhotoBinding


class PhotosRecyclerViewAdapter(private var photosList: List<Uri>, var context: Context):
    RecyclerView.Adapter<PhotosRecyclerViewAdapter.PhotoViewHolder>() {
    private val TAG: String = PhotosRecyclerViewAdapter::class.java.name

    inner class PhotoViewHolder(val cardPhotoBinding: CardPhotoBinding) :
        RecyclerView.ViewHolder(cardPhotoBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding: CardPhotoBinding = CardPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val currentPhoto: Uri = photosList[position]
        currentPhoto.let{
           Glide.with(context).load(it).into(holder.cardPhotoBinding.photo)
        }
    }

    override fun getItemCount(): Int {
        return photosList.size
    }

}