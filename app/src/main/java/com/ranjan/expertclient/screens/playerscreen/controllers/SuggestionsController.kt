package com.ranjan.expertclient.screens.playerscreen.controllers

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.PlayerScreenBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.VideosColumnAdapter
import com.ranjan.expertclient.screens.playerscreen.PlayerScreenViewModel
import com.ranjan.expertclient.screens.playerscreen.widgets.models.VideoDetailsAdapter

class SuggestionsController(
    private val binding: PlayerScreenBinding,
    private val psv: PlayerScreenViewModel,
    private val lifecycleOwner: LifecycleOwner
) {

    private val videosAdapter = VideosColumnAdapter(::onItemClick)
    private val detailsAdapter = VideoDetailsAdapter()

    fun setup() {
        val concat = ConcatAdapter(detailsAdapter, videosAdapter)

        binding.suggestionRecycler.apply {
            adapter = concat
            layoutManager = LinearLayoutManager(context)
        }

        binding.suggestionRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val lm = rv.layoutManager as LinearLayoutManager
                if (lm.findLastVisibleItemPosition() >= lm.itemCount - 2) {
                    psv.loadMoreSuggestions(null)
                }
            }
        })

        observe()
    }

    private fun observe() {
        psv.suggestionsList.observe(lifecycleOwner) {
            videosAdapter.submitList(it)
        }
    }

    private fun onItemClick(item: VideoItem) {
       /* psv.loadVideo(null, item, null, "")*/
    }
}