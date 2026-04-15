package com.ranjan.expertclient.screens.playerscreen.controllers

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.PlayerScreenBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.VideosColumnAdapter
import com.ranjan.expertclient.screens.playerscreen.PlayerScreenViewModel
import com.ranjan.expertclient.screens.playerscreen.widgets.models.VideoDetailsAdapter
import com.ranjan.expertclient.screens.playerscreen.widgets.videodetails.VideoDetailsHolder

class SuggestionsController(
    private val binding: PlayerScreenBinding,
    private val psv: PlayerScreenViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val visitorId: String,
    private val playerManager: PlayerManager
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
                    psv.loadMoreSuggestions(visitorId)
                }
            }
        })

        observe()
    }

    private fun observe() {
        psv.suggestionsList.observe(lifecycleOwner) {
            videosAdapter.submitList(it)
        }
        psv.videoDetails.observe(lifecycleOwner){details ->
            val holder = binding.suggestionRecycler.findViewHolderForAdapterPosition(0)
            if (holder is VideoDetailsHolder) {
                if (details!=null){
                    holder.bind(details) // manually update the view
                }

            }
        }
        psv.isLoading.observe(lifecycleOwner){isLoading->
            binding.loadingIndicator.visibility =
                if (isLoading) View.VISIBLE else View.GONE

        }
    }

    private fun onItemClick(item: VideoItem) {
        psv.loadVideo(
            videoItem = item,
            visitorId ,
            playerManager = playerManager
        )
        binding.suggestionRecycler.scrollToPosition(0)
    }
}