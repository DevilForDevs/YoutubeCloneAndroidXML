package com.ranjan.expertclient.screens.moviessitesclient.moviesfeeds.movieitem

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ranjan.expertclient.databinding.MovieItemHolderBinding
import com.ranjan.expertclient.models.VideoItem


class MovieItemHolder(
    private val binding: MovieItemHolderBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: VideoItem) {
        Glide.with(binding.imageView32)
            .load(item.thumbnail)
            .circleCrop()
            .into(binding.imageView32)
        binding.textView43.text = item.title
    }


}