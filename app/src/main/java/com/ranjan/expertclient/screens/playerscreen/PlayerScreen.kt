package com.ranjan.expertclient.screens.playerscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import com.ranjan.expertclient.databinding.PlayerScreenBinding
import com.ranjan.expertclient.screens.browserscreen.Store
import com.ranjan.expertclient.screens.playerscreen.controllers.DownloadAndPlay
import com.ranjan.expertclient.screens.playerscreen.controllers.FullscreenManager
import com.ranjan.expertclient.screens.playerscreen.controllers.PlayerManager
import com.ranjan.expertclient.screens.playerscreen.controllers.PlayerUIController
import com.ranjan.expertclient.screens.playerscreen.controllers.ResolutionDialog
import com.ranjan.expertclient.screens.playerscreen.controllers.SuggestionsController
import com.ranjan.expertclient.screens.playerscreen.models.DownloadItem
import com.ranjan.expertclient.screens.playerscreen.models.VideoDetails

@UnstableApi
class PlayerScreen : Fragment() {

    private lateinit var binding: PlayerScreenBinding
    private val sharedViewModel by activityViewModels<SharedVideoViewModel>()
    private val viewModel by activityViewModels<Store>()
    private val psv by activityViewModels<PlayerScreenViewModel>()
    private lateinit var playerManager: PlayerManager
    private val moviesViewModel by activityViewModels<MoviesViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PlayerScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val downloadModal= DownloadAndPlay(
            fragment = this,
            onActionClick = {
                moviesViewModel.action(it)
            },
            viewModel = moviesViewModel,
            play = ::play
        )

        playerManager = PlayerManager(
            context = requireContext(),
            psv = psv
        )

        val uiController = PlayerUIController(
            binding = binding,
            lifecycleOwner = viewLifecycleOwner,
            psv = psv,
            playerManager = playerManager,
            activity = requireActivity()
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

        sharedViewModel.selectedVideo.observe(viewLifecycleOwner){selectedVideo->
            if (selectedVideo.yt){
                psv.loadVideo(
                    videoItem = selectedVideo,
                    visitorId = viewModel.visitorId?:"",
                    playerManager
                )
            }else{
                moviesViewModel.loadVideo(selectedVideo,this.requireContext(), showDialog = {
                    downloadModal.show()
                },psv)

            }

        }

        psv.error.observe(viewLifecycleOwner){error->
            if (!error.isNullOrBlank()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }


        uiController.setup()
        fullscreenManager.setup()
        suggestionsController.setup()

        binding.playerUI.imageView17.setOnClickListener {
            if (sharedViewModel.selectedVideo.value?.yt==true){
                resolutionDialog.show(
                    psv.getResolutionList(),
                    psv.currentResolution.value
                ) {
                    playerManager.changeResolution(it)
                }
            }else{
                if (downloadModal.isShowing()){
                    downloadModal.dismiss()
                }else{
                    downloadModal.show()
                }

            }

        }

    }

    @OptIn(UnstableApi::class)
    override fun onPause() {
        super.onPause()
        playerManager.pause()
    }

    @OptIn(UnstableApi::class)
    override fun onDestroyView() {
        super.onDestroyView()
        playerManager.release()
    }
    fun channelClick(id: String) {
        val action = PlayerScreenDirections.actionPlayerScreenToChannelScreen(id)
        findNavController().navigate(action)
    }
    fun play(item: DownloadItem) {
        if (item.isFinished) {
            playerManager.playSimpleUrl(item.fileName)
        } else {
            playerManager.playSimpleUrl(item.fileUrl)
        }
    }




}


