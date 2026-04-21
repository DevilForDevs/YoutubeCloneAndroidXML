package com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.videos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.ChannelScreenTabBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.browserscreen.Store
import com.ranjan.expertclient.screens.playerscreen.SharedVideoViewModel
import com.ranjan.expertclient.screens.ytscreens.channelscreen.ChannelScreenViewModel
import com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.ViewModal

class VideosFragment : Fragment() {

    private lateinit var binding: ChannelScreenTabBinding
    private val viewModel by activityViewModels<ChannelScreenViewModel>()
    private val viewModal by activityViewModels<ViewModal>()
    private val store by activityViewModels<Store>()
    private val sharedViewModal by activityViewModels<SharedVideoViewModel>()



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
            val tab = tabs.find { it.title == "Videos" } ?: return@observe

            val recyclerHelper = RecyclerHelper(
                binding,
                viewLifecycleOwner,
                viewModal,
                ::onItemClick,
                store.visitorId ?: "",
                tab
            )
            recyclerHelper.setup()

            viewModal.loadVideos(store.visitorId ?: "", tab)
        }


    }

    fun onItemClick(item: VideoItem) {
        sharedViewModal.selectedVideo.value=item
        findNavController().navigate(R.id.action_channelScreen_to_playerScreen)

    }


}