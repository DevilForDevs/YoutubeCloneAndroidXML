package com.ranjan.expertclient.screens.playerscreen.widgets.videodetails

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ranjan.expertclient.databinding.PlayerScreenVideoDetailsBinding
import com.ranjan.expertclient.screens.playerscreen.widgets.models.VideoDetails

class VideoDetailsHolder(
    private val binding: PlayerScreenVideoDetailsBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(videoDetails: VideoDetails){
        println("loadimm")
        println(videoDetails)
       binding.textView11.text=videoDetails.title
        binding.textView14.text=videoDetails.localLizedViewsandUploadedAgo
        binding.textView15.text=videoDetails.firstHasTag
        binding.textView17.text=videoDetails.hashTags
        binding.apply {
            textView18.text=videoDetails.likes
            textView19.text=videoDetails.dislikes
            textView21.text=videoDetails.channelName
            textView23.text=videoDetails.subscriberCount
            textView26.text=videoDetails.commentsCount
        }
        Glide.with(binding.imageView24)
            .load(videoDetails.channelBigThumb)
            .into(binding.imageView24)

    }
}