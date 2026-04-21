package com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.playlist.widget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.PlaylistItemForChannelBinding
import com.ranjan.expertclient.models.VideoItem

val DiffCallback = object : DiffUtil.ItemCallback<VideoItem>() {

    override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
        return oldItem.videoId == newItem.videoId
    }

    override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
        return oldItem == newItem
    }
}

class PlaylistAdapter(
    private val playPlaylist:(item: VideoItem)-> Unit,
    private val viewPlaylist:(item:VideoItem)-> Unit
): ListAdapter<VideoItem,RecyclerView.ViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        p1: Int
    ): RecyclerView.ViewHolder {
        val binding = PlaylistItemForChannelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistItemHolder(binding)
    }

    override fun onBindViewHolder(
        p0: RecyclerView.ViewHolder,
        p1: Int
    ) {
        val item = getItem(p1)
        (p0 as PlaylistItemHolder).bind(
            item,
            playPlaylist = playPlaylist,
            viewPlaylist = viewPlaylist
        )
    }


}