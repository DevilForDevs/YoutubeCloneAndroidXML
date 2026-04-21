package com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.shorts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.ChannelScreenTabBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.browserscreen.Store
import com.ranjan.expertclient.screens.playerscreen.SharedVideoViewModel
import com.ranjan.expertclient.screens.ytscreens.channelscreen.ChannelScreenViewModel
import com.ranjan.expertclient.screens.ytscreens.channelscreen.models.ChannelTab


class ShortsFragment : Fragment() {

    private lateinit var binding: ChannelScreenTabBinding
    private val viewModel by activityViewModels<ChannelScreenViewModel>()
    private val store by activityViewModels<Store>()
    private val sharedViewModal by activityViewModels<SharedVideoViewModel>()
    private val viewModal by viewModels<ViewModal>()
    private var currentTab: ChannelTab? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChannelScreenTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.tabList.observe(viewLifecycleOwner) { tabs ->
            val tab = tabs.find { it.title == "Shorts" } ?: return@observe
            currentTab = tab

            if (binding.recycler.adapter == null) {
                val recyclerHelper = RecyclerHelper(
                    binding,
                    viewLifecycleOwner,
                    viewModal,
                    ::onItemClick,
                    store.visitorId ?: "",
                    { currentTab }
                )
                recyclerHelper.setup()
            }

            viewModal.loadVideos(store.visitorId ?: "", tab)
        }



    }
    fun onItemClick(item: VideoItem) {
        sharedViewModal.selectedVideo.value=item
        findNavController().navigate(R.id.action_channelScreen_to_playerScreen)

    }

}