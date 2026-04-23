package com.ranjan.expertclient.screens.moviessitesclient

import androidx.recyclerview.widget.DiffUtil
import com.ranjan.expertclient.models.VideoItem

class DiffCallback: DiffUtil.ItemCallback<VideoItem>() {

    override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
        return oldItem.videoId == newItem.videoId
    }

    override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
        return oldItem == newItem
    }
}