package com.ranjan.expertclient.screens.moviessitesclient.moviesfeeds.movieitem
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.MovieCategoryItemBinding
import com.ranjan.expertclient.databinding.MovieItemHolderBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.moviessitesclient.DiffCallback
import com.ranjan.expertclient.screens.moviessitesclient.moviesfeeds.movieitem.MovieCategoryHolder


class MovieAdapter(
    private val onItemClick:(item:VideoItem)-> Unit
) :  androidx.recyclerview.widget.ListAdapter<VideoItem, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType==2){
            val binding = MovieCategoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return MovieCategoryHolder(binding)
        }
        val binding = MovieItemHolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MovieItemHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
       if (getItem(position).category){
           (holder as MovieCategoryHolder).bind(getItem(position),onItemClick)
       }else{
           (holder as MovieItemHolder).bind(getItem(position),onItemClick)
       }
    }

    override fun getItemViewType(position: Int): Int {
        if (getItem(position).category){
            return 2
        }
        return 1
    }


}