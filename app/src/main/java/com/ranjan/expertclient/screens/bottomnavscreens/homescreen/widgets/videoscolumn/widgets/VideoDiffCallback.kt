package com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.widgets

import androidx.recyclerview.widget.DiffUtil
import com.ranjan.expertclient.models.VideoItem

object VideoDiffCallback : DiffUtil.ItemCallback<VideoItem>() {

    override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
        return oldItem.videoId == newItem.videoId
    }

    override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
        return oldItem == newItem
    }
}