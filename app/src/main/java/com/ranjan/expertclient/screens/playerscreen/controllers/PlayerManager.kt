package com.ranjan.expertclient.screens.playerscreen.controllers

import android.content.Context
import androidx.annotation.OptIn
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.ranjan.expertclient.screens.browserscreen.Store
import com.ranjan.expertclient.screens.playerscreen.PlayerScreenViewModel
import com.ranjan.expertclient.screens.playerscreen.SharedVideoViewModel
import com.ranjan.expertclient.screens.playerscreen.models.StreamItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    @OptIn(UnstableApi::class)
    fun play(formats: List<StreamItem>){
        val videoList = formats.filter { it.height != null }
        val audioList = formats.filter { it.height == null }

        val selectedVideo = videoList[0]
        psv.currentResolution.postValue("${selectedVideo.height}p") // ← add this line

        val selectedAudio = audioList.maxByOrNull { it.bitrate }

        selectedVideo.isSelected = true

        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(selectedVideo.url))
        val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(selectedAudio!!.url))

        val mergedSource = MergingMediaSource(videoSource, audioSource)

        player.setMediaSource(mergedSource)
        player.prepare()
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    val durationMs = player.duration
                    psv.totalDuration.postValue(durationMs)
                }
                if (state == Player.STATE_BUFFERING) {
                    psv.isLoading.postValue(true)
                }


            }
        })
        psv.startProgressUpdates(player)
        player.play()
        psv.isLoading.postValue(false)
        psv.isPaused.postValue(false)

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
        setResolution(
            resolution,

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

    @OptIn(UnstableApi::class)
    fun setResolution(resolution: String) {
        val selected = psv.adaptiveFormatsList
            .filter { it.height != null }
            .firstOrNull { "${it.height}p" == resolution } ?: return

        val audioSource = psv.adaptiveFormatsList
            .filter { it.height == null }
            .maxByOrNull { it.bitrate } ?: return

        val resumePos = player.currentPosition
        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(selected.url))
        val audio = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(audioSource.url))
        val merged = MergingMediaSource(videoSource, audio)
        player.setMediaSource(merged)
        player.prepare()
        player.seekTo(resumePos)
        player.play()
        psv.currentResolution.postValue(resolution)
    }
}