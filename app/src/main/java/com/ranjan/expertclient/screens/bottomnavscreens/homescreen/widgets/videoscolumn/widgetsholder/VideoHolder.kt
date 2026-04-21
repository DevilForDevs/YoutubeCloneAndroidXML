package com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.widgetsholder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ranjan.expertclient.databinding.HomeScreenVideosColumnVideoBinding
import com.ranjan.expertclient.models.VideoItem

class VideoHolder(
    private val binding: HomeScreenVideosColumnVideoBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: VideoItem,onItemClick:(item: VideoItem)-> Unit,onChannelClick:(id:String)-> Unit) {


        Glide.with(binding.imageView5)
            .load("https://i.ytimg.com/vi/${item.playlistId?:item.videoId}/hqdefault.jpg")
            .into(binding.imageView5)
        Glide.with(binding.imageView6)
            .load(item.channelAvtar)
            .circleCrop()
            .into(binding.imageView6)
        binding.textView3.text=item.title
        binding.textView4.text=item.channelName
        binding.textView5.text=item.views
        binding.imageView5.setOnClickListener {
            onItemClick(item)
        }
        binding.imageView6.setOnClickListener {
          onChannelClick(item.channelUrl?:"")
        }



    }
}