package com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.playlist

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.ChannelScreenTabBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.ytscreens.channelscreen.models.ChannelTab
import com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.playlist.widget.PlaylistAdapter

class RecyclerHelper(
    private val binding: ChannelScreenTabBinding,
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: ViewModal,
    private val onItemClick: (item: VideoItem) -> Unit,
    private val onBrowseClick:(item:VideoItem)-> Unit,
    private val visitorId: String,
    private val tabProvider: () -> ChannelTab?
) {
    fun setup(){
        val adapter= PlaylistAdapter(
            playPlaylist = onItemClick,
            viewPlaylist = onBrowseClick
        )
        binding.recycler.layoutManager = LinearLayoutManager(binding.root.context)
        binding.recycler.adapter=adapter
        viewModel.videosList.observe(lifecycleOwner){
            adapter.submitList(it)
        }
        viewModel.isLoading.observe(lifecycleOwner){
            binding.progressBar3.visibility=if (it) View.VISIBLE else View.GONE
        }
        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lm=recyclerView.layoutManager as LinearLayoutManager
                if (dy <= 0) return
                if (lm.itemCount == 0) return
                if (viewModel.isRequesting) return
                if (lm.findLastVisibleItemPosition()>=lm.itemCount-2){
                    val channelTab = tabProvider() ?: return
                    viewModel.loadContinuationItems(visitorId, channelTab)
                }
            }
        })


    }


}