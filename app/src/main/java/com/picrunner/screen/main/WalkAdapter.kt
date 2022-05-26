package com.picrunner.screen.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.picrunner.R
import com.picrunner.databinding.ItemLocationPhotoBinding

class WalkAdapter(private val glide: RequestManager) :
    ListAdapter<String, WalkAdapter.LocationPhotoViewHolder>(LocationUrlDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationPhotoViewHolder =
        LocationPhotoViewHolder(ItemLocationPhotoBinding.inflate(LayoutInflater.from(parent.context)))

    override fun onBindViewHolder(holder: LocationPhotoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class LocationPhotoViewHolder (private val binding: ItemLocationPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(url: String) {
            binding.apply {
                glide.load(url)
                    .placeholder(R.drawable.ic_photo_placeholder)
                    .dontAnimate()
                    .centerCrop()
                    .into(cardPhoto)
            }
        }
    }
}

class LocationUrlDiffCallback : DiffUtil.ItemCallback<String>() {

    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}
