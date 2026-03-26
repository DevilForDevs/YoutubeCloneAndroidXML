package com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.widgets

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ranjan.expertclient.databinding.HomeScreenVideosColumnVideoBinding
import com.ranjan.expertclient.models.VideoItem

class VideoHolder(
    private val binding: HomeScreenVideosColumnVideoBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: VideoItem) {


        if (item.playlistId==null){
            Glide.with(binding.imageView5)
                .load("https://i.ytimg.com/vi/${item.videoId}/hqdefault.jpg")
                .into(binding.imageView5)
            println("binded imgv $item")

            Glide.with(binding.imageView6)
                .load(item.channelAvtar)
                .circleCrop()
                .into(binding.imageView6)
            binding.textView3.text=item.title
            binding.textView4.text=item.channelName
            binding.textView5.text=item.views
        }


    }
}