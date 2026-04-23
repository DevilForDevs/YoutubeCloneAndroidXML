package com.ranjan.expertclient.screens.commanscreens.feeds_screen

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import com.ranjan.expertclient.databinding.FeedsScreenBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.VideosColumnAdapter

class RecyclerViewHelper(
    private val binding: FeedsScreenBinding,
    private val viewModel: FeedsScreenViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val onItemClick:(item: VideoItem)-> Unit,
    private val onChannelClick:(browseId: String)-> Unit,
    private val fragment: FeedsScreen
) {
    private val videosAdapter= VideosColumnAdapter(onItemClick,onChannelClick)

    fun setup(){
        binding.feedsRecycler.apply {
            adapter=videosAdapter
            layoutManager= GridLayoutManager(fragment.context,2)
        }
        viewModel.feedsList.observe(lifecycleOwner){items ->
            videosAdapter.submitList(items)
        }
    }
}
