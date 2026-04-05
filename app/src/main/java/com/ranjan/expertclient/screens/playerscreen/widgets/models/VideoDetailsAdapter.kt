package com.ranjan.expertclient.screens.playerscreen.widgets.models

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.PlayerScreenVideoDetailsBinding
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.categoryrow.CategoryRowHolder
import com.ranjan.expertclient.screens.playerscreen.widgets.videodetails.VideoDetailsHolder

class VideoDetailsAdapter(
) : RecyclerView.Adapter<VideoDetailsHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VideoDetailsHolder {
        val binding= PlayerScreenVideoDetailsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoDetailsHolder(binding)
    }

    override fun onBindViewHolder(
        holder: VideoDetailsHolder,
        position: Int
    ) {

    }

    override fun getItemCount(): Int = 1
}