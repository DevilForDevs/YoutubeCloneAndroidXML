package com.ranjan.expertclient.screens.playerscreen.widgets.videodetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.PlayerScreenVideoDetailsBinding

class VideoDetailsAdapter(
    private val channelClick:(id:String)-> Unit
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
        return VideoDetailsHolder(binding,channelClick)
    }

    override fun onBindViewHolder(
        holder: VideoDetailsHolder,
        position: Int
    ) {

    }

    override fun getItemCount(): Int = 1
}