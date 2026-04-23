package com.ranjan.expertclient.screens.commanscreens.feeds_screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.ranjan.expertclient.databinding.FeedsScreenBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.playerscreen.SharedVideoViewModel
import kotlin.getValue

class FeedsScreen : Fragment() {

    private lateinit var binding: FeedsScreenBinding
    private val viewModel by activityViewModels<FeedsScreenViewModel>()
    private val sharedViewModel by activityViewModels<SharedVideoViewModel>()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FeedsScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
        val helper= RecyclerViewHelper(
            binding = binding,
            viewModel = viewModel,
            lifecycleOwner =viewLifecycleOwner,
            onItemClick = ::onItemClick,
            onChannelClick =::onChannelClick,
            fragment = this
        )
        helper.setup()
        viewModel.loadingFeeds(sharedViewModel.selectedSite.value!!)

    }
    fun onItemClick(item: VideoItem){

    }
    fun onChannelClick(browseId: String){

    }

}