package com.ranjan.expertclient.screens.ytscreens.searchscreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.YtSearchScreenBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.VideosColumnAdapter
import com.ranjan.expertclient.screens.playerscreen.SharedVideoViewModel
import com.ranjan.expertclient.screens.ytscreens.searchscreen.controllers.SearchController
import kotlin.getValue

class SearchScreen : Fragment() {

    private lateinit var binding: YtSearchScreenBinding
    private val sharedViewModel by activityViewModels<SharedVideoViewModel>()
    private val ssvm by activityViewModels<SearchScreenViewModel>()
    val videosAdapter = VideosColumnAdapter(::onItemClick,::onChannelClick)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = YtSearchScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
        val searchController= SearchController(
            binding, back = {
                findNavController().popBackStack()
            }, videosAdapter, ssvm,
            lifecycleOwner = viewLifecycleOwner
        )



    }

    fun onItemClick(item: VideoItem){
        sharedViewModel.selectedVideo.value=item
       findNavController().navigate(R.id.action_searchScreen_to_playerScreen)

    }
    private fun onChannelClick(id: String){

    }

}