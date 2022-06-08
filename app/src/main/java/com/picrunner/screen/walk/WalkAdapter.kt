package com.picrunner.screen.walk

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.picrunner.R
import com.picrunner.databinding.ItemPhotoBinding
import com.picrunner.domain.model.Photo
import com.picrunner.util.convertToStringUrl

class WalkAdapter(private val glide: RequestManager) :
    ListAdapter<Photo, WalkAdapter.LocationPhotoViewHolder>(LocationUrlDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationPhotoViewHolder =
        LocationPhotoViewHolder(ItemPhotoBinding.inflate(LayoutInflater.from(parent.context)))

    override fun onBindViewHolder(holder: LocationPhotoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class LocationPhotoViewHolder (private val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: Photo) {
            binding.apply {
                glide.load(photo.convertToStringUrl())
                    .optionalFitCenter()
                    .optionalCenterCrop()
                    .placeholder(R.drawable.ic_photo_placeholder)
                    .into(cardPhoto)
            }
        }
    }
}

class LocationUrlDiffCallback : DiffUtil.ItemCallback<Photo>() {

    override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
        return oldItem.title == newItem.title
    }
}
