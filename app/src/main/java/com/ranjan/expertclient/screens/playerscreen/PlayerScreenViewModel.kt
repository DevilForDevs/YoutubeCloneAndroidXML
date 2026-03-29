package com.ranjan.expertclient.screens.playerscreen

import androidx.annotation.OptIn
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.ranjan.expertclient.apiendpoints.getStreamingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

data class StreamItem(
    val itag: Int,
    val url: String,
    val mimeType: String,
    val height: Int?,
    val bitrate: Int,
    var isSelected: Boolean = false
)

class PlayerScreenViewModel : ViewModel() {

    val isLoading = MutableLiveData(false)
    val error = MutableLiveData<String?>()

    var adaptiveFormatsList: List<StreamItem> = emptyList()

    @OptIn(UnstableApi::class)
    fun loadVideo(
        player: ExoPlayer,
        sharedVideoViewModel: SharedVideoViewModel,
        dsf: DefaultDataSource.Factory,
        preferredHeight: Int? = null
    ) {
        isLoading.value = true
        error.value = null

        // Fully stop & release old playback
        player.stop()
        player.clearMediaItems()
        player.playWhenReady = false



        viewModelScope.launch {
            try {
                adaptiveFormatsList = withContext(Dispatchers.IO) {
                    val streamingData = getStreamingData(
                        sharedVideoViewModel.selectedVideo.value.videoId
                    )
                    val array = streamingData
                        .getJSONObject("playerResponse")
                        .getJSONObject("streamingData")
                        .getJSONArray("adaptiveFormats")
                    parseAdaptiveFormats(array)
                }

                val videoList = adaptiveFormatsList.filter { it.height != null }
                val audioList = adaptiveFormatsList.filter { it.height == null }

                if (videoList.isEmpty() || audioList.isEmpty()) {
                    isLoading.postValue(false)
                    error.postValue("No playable stream found")
                    return@launch
                }

                val selectedVideo = preferredHeight?.let { h ->
                    videoList.find { it.height == h } ?: videoList.maxByOrNull { it.height!! }
                } ?: videoList.maxByOrNull { it.height!! }

                val selectedAudio = audioList.maxByOrNull { it.bitrate }

                selectedVideo?.isSelected = true

                val videoSource = ProgressiveMediaSource.Factory(dsf)
                    .createMediaSource(MediaItem.fromUri(selectedVideo!!.url))
                val audioSource = ProgressiveMediaSource.Factory(dsf)
                    .createMediaSource(MediaItem.fromUri(selectedAudio!!.url))

                val mergedSource = MergingMediaSource(videoSource, audioSource)

                // ✅ Main thread for playback
                withContext(Dispatchers.Main) {
                    // Completely detach old player from view first
                    player.setMediaSource(mergedSource)
                    player.prepare()
                    player.play()
                    isLoading.postValue(false)
                }

            } catch (e: Exception) {
                isLoading.postValue(false)
                error.postValue(e.message)
            }
        }
    }


    private fun parseAdaptiveFormats(array: JSONArray): List<StreamItem> {
        val list = mutableListOf<StreamItem>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            val itag = obj.optInt("itag")
            val url = obj.optString("url")
            val mimeType = obj.optString("mimeType")
            val height = if (obj.has("height")) obj.optInt("height") else null
            val bitrate = obj.optInt("bitrate")

            if (url.isNullOrEmpty()) continue

            val isVideoAvc = mimeType.contains("video") && mimeType.contains("avc1")
            val isAudio = mimeType.contains("audio")

            if (!(isVideoAvc || isAudio)) continue

            list.add(
                StreamItem(
                    itag = itag,
                    url = url,
                    mimeType = mimeType,
                    height = height,
                    bitrate = bitrate
                )
            )
        }

        return list
    }


}