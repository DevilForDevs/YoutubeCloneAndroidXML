package com.ranjan.expertclient.screens.moviessitesclient.moviesfeeds

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.ranjan.expertclient.databinding.MoviesFeedsScreenBinding
import com.ranjan.expertclient.screens.moviessitesclient.moviesfeeds.movieitem.MovieAdapter

class RecyclerHelper(
    private val binding: MoviesFeedsScreenBinding,
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: MoviesScreenViewModel,
    private val fragment: MoviesFeedsScreen
) {
    fun setup(){
        val movieItemAdapter= MovieAdapter()
        binding.movieRecycler.apply {
            adapter=movieItemAdapter
            layoutManager= LinearLayoutManager(fragment.requireContext(), LinearLayoutManager.VERTICAL,false)
        }
        viewModel.moviesList.observe(lifecycleOwner){items ->
            movieItemAdapter.submitList(items)
        }
        viewModel.loading.observe(lifecycleOwner){loading->
            binding.progressBar6.visibility=if (loading)View.VISIBLE else View.INVISIBLE
        }

    }
}
