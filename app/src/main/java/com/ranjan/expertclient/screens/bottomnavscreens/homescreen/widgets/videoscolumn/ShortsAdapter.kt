package com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.HomeScreenShortItemBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.widgets.ShortItemHolder

class ShortsAdapter : ListAdapter<VideoItem, ShortItemHolder>(VideoDiffCallback()) {

    fun setData(list: List<VideoItem>) {
        submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortItemHolder {
        val binding = HomeScreenShortItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShortItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ShortItemHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class VideoDiffCallback : DiffUtil.ItemCallback<VideoItem>() {
        override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem.videoId == newItem.videoId
        }

        override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem == newItem
        }
    }
}