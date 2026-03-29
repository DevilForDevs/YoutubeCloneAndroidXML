package com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.widgetsholder

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.ShortsArrayHomeScreenBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.ShortsAdapter

class ShortsArrayHolder(
    private val binding: ShortsArrayHomeScreenBinding,
) : RecyclerView.ViewHolder(binding.root) {

    private val adapter = ShortsAdapter()

    init {
        binding.recyclerShorts.apply {
            layoutManager = LinearLayoutManager(
                binding.root.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = this@ShortsArrayHolder.adapter

            // performance
            setHasFixedSize(true)

            // optional: remove nested scroll issues
            isNestedScrollingEnabled = false
        }
    }
    fun bind(shortsList: MutableList<VideoItem>?) {
        if (shortsList.isNullOrEmpty()) return
        adapter.setData(shortsList)
    }

}