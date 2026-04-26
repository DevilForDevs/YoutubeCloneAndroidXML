package com.ranjan.expertclient.screens.playerscreen.controllers

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.ranjan.expertclient.screens.playerscreen.PlayerScreenViewModel
import com.ranjan.expertclient.screens.playerscreen.models.StreamItem
import com.ranjan.expertclient.utils.getOkHttpClient

@UnstableApi
class PlayerManager(
    context: Context,
    private val psv: PlayerScreenViewModel,

) {

    companion object {
        private const val TAG = "PlayerManager"
    }

    val player: ExoPlayer = ExoPlayer.Builder(context).build()

    @OptIn(UnstableApi::class)
    private val dataSourceFactory = DefaultDataSource.Factory(
        context,
        OkHttpDataSource.Factory(getOkHttpClient())
            .setDefaultRequestProperties(
                mapOf(
                    "User-Agent" to "Mozilla/5.0",
                    "Accept" to "*/*",
                    "Accept-Encoding" to "identity" // avoids gzip/range issues
                )
            )
    )

    private val playbackListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_READY -> {
                    psv.totalDuration.postValue(player.duration)
                }

                Player.STATE_BUFFERING -> {
                    psv.isLoading.postValue(true)
                }

                Player.STATE_ENDED,
                Player.STATE_IDLE -> {
                    psv.isLoading.postValue(false)
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "Playback error", error)
            psv.error.postValue(error.message ?: "Playback error")
            psv.isLoading.postValue(false)
            psv.isPaused.postValue(true)
        }
    }

    init {
        player.addListener(playbackListener)
    }

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

        val mergedSource = MergingMediaSource(true, videoSource, audioSource)

        player.setMediaSource(mergedSource)
        player.prepare()
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
        player.removeListener(playbackListener)
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
        val merged = MergingMediaSource(true, videoSource, audio)
        player.setMediaSource(merged)
        player.prepare()
        player.seekTo(resumePos)
        player.play()
        psv.currentResolution.postValue(resolution)
    }

    @OptIn(UnstableApi::class)
    fun playSimpleUrl(url: String) {
        psv.resetSimplePlaybackState()
        player.stop()

        val mediaItem = MediaItem.fromUri(url)
        val source = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)


        player.setMediaSource(source)
        player.prepare()
        psv.startProgressUpdates(player)
        player.play()
        psv.isPaused.postValue(false)

    }
}