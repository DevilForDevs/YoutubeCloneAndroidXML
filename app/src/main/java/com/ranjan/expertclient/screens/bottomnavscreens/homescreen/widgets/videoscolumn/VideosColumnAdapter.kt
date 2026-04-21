package com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.HomeScreenVideosColumnVideoBinding
import com.ranjan.expertclient.databinding.ShortsArrayHomeScreenBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.widgetsholder.ShortsArrayHolder
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.widgetsholder.VideoHolder

class VideosColumnAdapter(
    private val onItemClick:(item: VideoItem)-> Unit,
    private val onChannelClick:(id:String)-> Unit
):
    androidx.recyclerview.widget.ListAdapter<VideoItem, RecyclerView.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):RecyclerView.ViewHolder {
        if (viewType==2){
            val binding = ShortsArrayHomeScreenBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
          return ShortsArrayHolder(binding)
        }
        val binding = HomeScreenVideosColumnVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item=getItem(position)
        if (item.shortsArray!=null){
            (holder as ShortsArrayHolder).bind(item.shortsArray)
        }
        if (item.shortsArray==null){
            (holder as VideoHolder).bind(item,onItemClick,onChannelClick)
        }

    }

    override fun getItemViewType(position: Int): Int {
        if (getItem(position).shortsArray!=null){
            return 2
        }
        return 1
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<VideoItem>() {

            override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
                return oldItem.videoId == newItem.videoId
            }

            override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}