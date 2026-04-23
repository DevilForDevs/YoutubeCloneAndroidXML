package com.ranjan.expertclient.screens.moviessitesclient.moviesfeeds

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.ranjan.expertclient.databinding.MoviesFeedsScreenBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.moviessitesclient.moviesfeeds.movieitem.MovieAdapter
import com.ranjan.expertclient.screens.playerscreen.SharedVideoViewModel
import java.io.PipedReader

class RecyclerHelper(
    private val binding: MoviesFeedsScreenBinding,
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: MoviesScreenViewModel,
    private val fragment: MoviesFeedsScreen,
    private val onItemClick:(item:VideoItem)-> Unit,
    private val sharedViewModel: SharedVideoViewModel
) {
    fun setup(){
        val movieItemAdapter= MovieAdapter(onItemClick)
        binding.movieRecycler.apply {
            adapter=movieItemAdapter
            layoutManager= LinearLayoutManager(fragment.requireContext(), LinearLayoutManager.VERTICAL,false)
        }
        viewModel.state.observe(lifecycleOwner) { state ->

            movieItemAdapter.submitList(state.currentPage.items)

            binding.progressBar6.visibility =
                if (state.isLoading) View.VISIBLE else View.GONE
        }

        viewModel.state.observe(lifecycleOwner) { state ->
            val selectedTitle = sharedViewModel.selectedSite.value?.title?.trim().orEmpty()
            val currentTitle = state.currentPage.title?.trim().orEmpty()
            binding.siteTitleText.text = when {
                currentTitle.equals("Loading", ignoreCase = true) && selectedTitle.isNotBlank() -> selectedTitle
                currentTitle.equals("Home", ignoreCase = true) && selectedTitle.isNotBlank() -> selectedTitle
                currentTitle.isNotBlank() -> currentTitle
                selectedTitle.isNotBlank() -> selectedTitle
                else -> "Movies"
            }
        }

    }
}
