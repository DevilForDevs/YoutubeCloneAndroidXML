package com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.widgets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.ranjan.expertclient.databinding.HomeScreenVideosColumnVideoBinding
import com.ranjan.expertclient.models.VideoItem

class VideosAdapter :
    ListAdapter<VideoItem, VideoHolder>(VideoDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        val binding = HomeScreenVideosColumnVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        holder.bind(getItem(position))
    }
}