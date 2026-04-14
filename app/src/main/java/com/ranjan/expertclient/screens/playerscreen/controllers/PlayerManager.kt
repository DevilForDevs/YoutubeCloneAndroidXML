package com.ranjan.expertclient.screens.playerscreen.controllers

import android.content.Context
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.ranjan.expertclient.screens.browserscreen.Store
import com.ranjan.expertclient.screens.playerscreen.PlayerScreenViewModel
import com.ranjan.expertclient.screens.playerscreen.SharedVideoViewModel

class PlayerManager(
    private val context: Context,
    private val psv: PlayerScreenViewModel,
    private val sharedViewModel: SharedVideoViewModel,
    private val viewModel: Store
) {

    val player: ExoPlayer = ExoPlayer.Builder(context).build()
    private val dataSourceFactory = DefaultDataSource.Factory(context)

    fun attach(playerView: PlayerView) {
        playerView.player = player
    }

    fun loadInitial() {
        psv.loadVideo(
            player,
            sharedViewModel.selectedVideo.value,
            dataSourceFactory,
            viewModel.visitorId ?: ""
        )
    }

    fun togglePlayback() {
        if (player.isPlaying) {
            player.pause()
            psv.isPaused.postValue(true)
        } else {
            player.play()
            psv.isPaused.postValue(false)
        }
    }

    fun changeResolution(resolution: String) {
        psv.setResolution(
            player,
            resolution,
            dataSourceFactory,
            viewModel.visitorId ?: ""
        )
    }

    fun seekTo(position: Long) {
        psv._isSeeking.value = true
        player.seekTo(position)
        psv._isSeeking.value = false
    }

    fun pause() {
        player.pause()
    }

    fun release() {
        player.release()
    }
}