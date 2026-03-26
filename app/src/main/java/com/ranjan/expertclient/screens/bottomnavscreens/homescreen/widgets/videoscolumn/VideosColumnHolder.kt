package com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.HomeScreenVideosColumnBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.widgets.VideosAdapter

class VideosColumnHolder(
    private val binding: HomeScreenVideosColumnBinding
) : RecyclerView.ViewHolder(binding.root) {

    private val adapter = VideosAdapter()

    init {
        binding.homeScreenVideosRecycler.layoutManager =
            LinearLayoutManager(binding.root.context)

        binding.homeScreenVideosRecycler.adapter = adapter


        // important for nested recycler
        binding.homeScreenVideosRecycler.isNestedScrollingEnabled = false
    }

    fun bind(videos: List<VideoItem>) {
        adapter.submitList(videos)
    }



}