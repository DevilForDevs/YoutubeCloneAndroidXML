package com.ranjan.expertclient.screens.playerscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.ranjan.expertclient.databinding.PlayerScreenBinding

class PlayerScreen : Fragment() {

    private var player: ExoPlayer?=null
    private lateinit var binding: PlayerScreenBinding
    private val sharedViewModel by activityViewModels<SharedVideoViewModel>()
    private val psv by activityViewModels<PlayerScreenViewModel>()
    lateinit var dataSourceFactory: DefaultDataSource.Factory
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
        dataSourceFactory = DefaultDataSource.Factory(requireContext())
        player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player=player
        psv.loadVideo(player!!,sharedViewModel,dataSourceFactory)
        binding.playerUI.imageView17.setOnClickListener {
            ::showResolutionsModal
        }
        psv.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.loadingIndicator.visibility =
                if (loading) View.VISIBLE else View.GONE
        }

    }
    fun showResolutionsModal(){

    }

    override fun onPause() {
        super.onPause()
        player?.stop()
    }

}