package com.ranjan.expertclient.screens.moviessitesclient.moviesfeeds.movieitem

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ranjan.expertclient.databinding.MovieCategoryItemBinding
import com.ranjan.expertclient.models.VideoItem

class MovieCategoryHolder(
    private val binding: MovieCategoryItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: VideoItem,onItemClick:(item:VideoItem)-> Unit) {
        binding.textView45.text = item.title
        binding.root.setOnClickListener {
            onItemClick(item)
        }

    }



}