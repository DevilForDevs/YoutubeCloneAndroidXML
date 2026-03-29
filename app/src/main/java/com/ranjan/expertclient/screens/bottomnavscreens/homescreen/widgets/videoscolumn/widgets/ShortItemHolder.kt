package com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.widgets

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ranjan.expertclient.databinding.HomeScreenShortItemBinding
import com.ranjan.expertclient.models.VideoItem

class ShortItemHolder(
    private val binding: HomeScreenShortItemBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(shortItem: VideoItem){
        Glide.with(binding.imageView12)
            .load("https://i.ytimg.com/vi/${shortItem.videoId}/hqdefault.jpg")
            .into(binding.imageView12)
        binding.textView9.text=shortItem.views
        binding.textView10.text=shortItem.title
    }

}