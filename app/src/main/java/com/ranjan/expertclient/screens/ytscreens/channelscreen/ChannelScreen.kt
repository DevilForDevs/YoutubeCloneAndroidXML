package com.ranjan.expertclient.screens.ytscreens.channelscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.ChannelScreenBinding
import com.ranjan.expertclient.screens.browserscreen.Store
import com.ranjan.expertclient.screens.ytscreens.channelscreen.controllers.TabsController
import kotlin.getValue

class ChannelScreen : Fragment() {

    private lateinit var binding: ChannelScreenBinding
    private lateinit var tabsController: TabsController
    private val args: ChannelScreenArgs by navArgs()
    private val viewModel by activityViewModels<ChannelScreenViewModel>()
    private val store by activityViewModels<Store>()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChannelScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }

        viewModel.loadTabs(args.id,store.visitorId?:"")
        tabsController = TabsController(binding, this,viewLifecycleOwner,viewModel)
        tabsController.setup()

        binding.textView34.setOnClickListener {
            findNavController().navigate(R.id.action_channelScreen_to_playlistScreen)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}