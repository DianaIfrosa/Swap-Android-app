package com.diana.bachelorthesis.adapters

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.diana.bachelorthesis.databinding.CardPhotoBinding
import com.squareup.picasso.Picasso

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
        Log.d(TAG, "on Bind for position $position")
        val currentPhoto: Uri = photosList[position]
        currentPhoto.let{
            Picasso.get().load(it).into(holder.cardPhotoBinding.photo)
        }
    }

    override fun getItemCount(): Int {
        return photosList.size
    }

}