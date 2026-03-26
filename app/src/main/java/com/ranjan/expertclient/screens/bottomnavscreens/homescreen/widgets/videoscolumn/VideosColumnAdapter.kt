package com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.HomeScreenVideosColumnBinding
import com.ranjan.expertclient.models.VideoItem

class VideosColumnAdapter(
    private val videos: List<VideoItem>
) : RecyclerView.Adapter<VideosColumnHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosColumnHolder {
        val binding = HomeScreenVideosColumnBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideosColumnHolder(binding)
    }

    override fun onBindViewHolder(holder: VideosColumnHolder, position: Int) {
        holder.bind(videos)
    }


    override fun getItemCount(): Int = 1
}