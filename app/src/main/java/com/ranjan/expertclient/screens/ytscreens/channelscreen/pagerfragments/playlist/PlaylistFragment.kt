package com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.ranjan.expertclient.databinding.ChannelScreenTabBinding
import com.ranjan.expertclient.screens.ytscreens.channelscreen.ChannelScreenViewModel

class PlaylistFragment : Fragment() {

    private lateinit var binding: ChannelScreenTabBinding
    private val viewModel by activityViewModels<ChannelScreenViewModel>()
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

    }

}