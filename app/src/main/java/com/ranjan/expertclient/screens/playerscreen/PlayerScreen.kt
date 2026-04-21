package com.ranjan.expertclient.screens.playerscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ranjan.expertclient.databinding.PlayerScreenBinding
import com.ranjan.expertclient.screens.browserscreen.Store
import com.ranjan.expertclient.screens.playerscreen.controllers.FullscreenManager
import com.ranjan.expertclient.screens.playerscreen.controllers.PlayerManager
import com.ranjan.expertclient.screens.playerscreen.controllers.PlayerUIController
import com.ranjan.expertclient.screens.playerscreen.controllers.ResolutionDialog
import com.ranjan.expertclient.screens.playerscreen.controllers.SuggestionsController

class PlayerScreen : Fragment() {

    private lateinit var binding: PlayerScreenBinding
    private val sharedViewModel by activityViewModels<SharedVideoViewModel>()
    private val viewModel by activityViewModels<Store>()
    private val psv by activityViewModels<PlayerScreenViewModel>()
    private lateinit var playerManager: PlayerManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PlayerScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playerManager = PlayerManager(
            context = requireContext(),
            psv = psv,
            sharedViewModel = sharedViewModel,
            viewModel = viewModel
        )

        val uiController = PlayerUIController(
            binding = binding,
            lifecycleOwner = viewLifecycleOwner,
            psv = psv,
            playerManager = playerManager
        )
        val fullscreenManager = FullscreenManager(
            activity = requireActivity(),
            binding = binding,
            psv = psv,
            lifecycleOwner = viewLifecycleOwner
        )

        ViewCompat.setOnApplyWindowInsetsListener(view) { rootView, insets ->
            val isFull = psv.isFullScreen.value == true
            val statusBarHeight = if (isFull) 0 else insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            rootView.setPadding(0, statusBarHeight, 0, 0)
            if (isFull) {
                fullscreenManager.reapplyImmersiveIfNeeded()
            }
            insets
        }

        val suggestionsController = SuggestionsController(
            binding = binding,
            psv = psv,
            lifecycleOwner = viewLifecycleOwner,
            viewModel.visitorId?:"",
            playerManager,
            ::channelClick
        )
        val resolutionDialog = ResolutionDialog(this)

        playerManager.attach(binding.playerView)

        //load inital video
        psv.loadVideo(
            videoItem = sharedViewModel.selectedVideo.value!!,
            visitorId = viewModel.visitorId?:"",
            playerManager
        )

        uiController.setup()
        fullscreenManager.setup()
        suggestionsController.setup()

        binding.playerUI.imageView17.setOnClickListener {
            resolutionDialog.show(
                psv.getResolutionList(),
                psv.currentResolution.value
            ) {
                playerManager.changeResolution(it)
            }
        }

    }

    override fun onPause() {
        super.onPause()
        playerManager.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        playerManager.release()
    }
    fun channelClick(id: String) {
        val action = PlayerScreenDirections.actionPlayerScreenToChannelScreen(id)
        findNavController().navigate(action)
    }

}


