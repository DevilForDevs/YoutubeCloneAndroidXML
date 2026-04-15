package com.ranjan.expertclient.screens.playerscreen.widgets.videodetails

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ranjan.expertclient.databinding.PlayerScreenVideoDetailsBinding
import com.ranjan.expertclient.screens.playerscreen.widgets.models.VideoDetails

class VideoDetailsHolder(
    private val binding: PlayerScreenVideoDetailsBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(videoDetails: VideoDetails){
        binding.textView11.text=videoDetails.title
        binding.textView14.text=videoDetails.localLizedViewsandUploadedAgo
        binding.textView15.text=videoDetails.firstHasTag
        binding.textView17.text=videoDetails.hashTags
        binding.apply {
            textView21.text=videoDetails.channelName
            textView23.text=videoDetails.subscriberCount+" subscribers"
            textView26.text=videoDetails.commentsCount
            actionLayout.likes.text=videoDetails.likes
        }
        Glide.with(binding.imageView24)
            .load(videoDetails.channelBigThumb)
            .into(binding.imageView24)

    }
}