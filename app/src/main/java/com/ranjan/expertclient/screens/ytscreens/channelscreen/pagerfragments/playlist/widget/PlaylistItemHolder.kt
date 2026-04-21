package com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.playlist.widget

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ranjan.expertclient.databinding.PlaylistItemForChannelBinding
import com.ranjan.expertclient.models.VideoItem

class PlaylistItemHolder(
    private val binding: PlaylistItemForChannelBinding
): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: VideoItem,playPlaylist:(item: VideoItem)-> Unit,viewPlaylist:(item:VideoItem)-> Unit){
        Glide.with(binding.imageView31)
            .load("https://i.ytimg.com/vi/${item.playlistId?:item.videoId}/hqdefault.jpg")
            .into(binding.imageView31)
        binding.textView37.text=item.title
        binding.textView39.text=item.views
        
        binding.root.setOnClickListener {
            viewPlaylist(item)
        }
        binding.imageView31.setOnClickListener {
           playPlaylist(item)
        }
    }
}