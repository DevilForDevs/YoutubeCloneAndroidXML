package com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.HomeScreenShortItemBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.widgets.ShortItemHolder

class ShortsAdapter : RecyclerView.Adapter<ShortItemHolder>() {

    private val items = mutableListOf<VideoItem>()

    fun setData(list: List<VideoItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
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
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}