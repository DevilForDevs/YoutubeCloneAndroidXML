package com.ranjan.expertclient.screens.playerscreen

import androidx.annotation.OptIn
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.ranjan.expertclient.apiendpoints.getStreamingData
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.browserscreen.parsers.parseInitialData
import com.ranjan.expertclient.screens.playerscreen.utils.WatchNextBrowse
import com.ranjan.expertclient.screens.playerscreen.utils.parseAdaptiveFormats
import com.ranjan.expertclient.screens.playerscreen.utils.parseWatchHtml
import com.ranjan.expertclient.screens.playerscreen.widgets.models.VideoDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val isPaused = MutableLiveData(false)
    val isFullScreen = MutableLiveData(false)
    val error = MutableLiveData<String?>()
    val totalDuration = MutableLiveData<Long?>()
    val durationProgress = MutableLiveData<Long?>()
    val videoDetails = MutableLiveData<VideoDetails?>()
    private val suggestions = MutableLiveData<MutableList<VideoItem>>(mutableListOf())
    val suggestionsList: LiveData<MutableList<VideoItem>> get() = suggestions
    var adaptiveFormatsList: List<StreamItem> = emptyList()
    val showControls = MutableLiveData(true)
    var progressJob: Job? = null
    var continuation: String?=null

    private var isRequestInFlight = false
    private  val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 15; CPH2665 Build/AP3A.240617.008; wv) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 " +
                "Chrome/143.0.7499.34 Mobile Safari/537.36"


    fun toggleFullScreen() {
        isFullScreen.value = !(isFullScreen.value ?: false)
    }
    fun startProgressUpdates(player: ExoPlayer) {
        progressJob?.cancel() // stop old loop if any

        progressJob = viewModelScope.launch{
            while (isActive) {
                if (player.isPlaying){
                    val pos = player.currentPosition
                    isLoading.postValue(false)
                    durationProgress.postValue(pos)
                }
                delay(500)

            }
        }
    }
    fun togglePlayBack(player: Player){
        if (player.isPlaying) {
            player.pause() // ✅ NOT stop()
            isPaused.postValue(true)
        } else {
            player.play()
            isPaused.postValue(false)
        }
    }
    fun stopProgressUpdates() {
        progressJob?.cancel()
    }
    @OptIn(UnstableApi::class)
    fun loadVideo(
        player: ExoPlayer,
        videoItem: VideoItem,
        dsf: DefaultDataSource.Factory,
        visitorId: String
    ) {
        isPaused.value=true
        isLoading.value = true
        error.value = null
        totalDuration.postValue(0L)
        durationProgress.postValue(0L)

        // Fully stop & release old playback
        player.stop()
        stopProgressUpdates()
        player.clearMediaItems()
        player.playWhenReady = false

        viewModelScope.launch {
            try {

                adaptiveFormatsList = withContext(Dispatchers.IO) {
                    val streamingData = getStreamingData(
                        videoItem.videoId,
                        visitorData = visitorId
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

                val selectedVideo = videoList[0]

                val selectedAudio = audioList.maxByOrNull { it.bitrate }

                selectedVideo.isSelected = true

                val videoSource = ProgressiveMediaSource.Factory(dsf)
                    .createMediaSource(MediaItem.fromUri(selectedVideo.url))
                val audioSource = ProgressiveMediaSource.Factory(dsf)
                    .createMediaSource(MediaItem.fromUri(selectedAudio!!.url))

                val mergedSource = MergingMediaSource(videoSource, audioSource)

                // ✅ Main thread for playback
                withContext(Dispatchers.Main) {
                    // Completely detach old player from view first
                    player.setMediaSource(mergedSource)
                    player.prepare()


                    player.addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            if (state == Player.STATE_READY) {
                                val durationMs = player.duration
                                totalDuration.postValue(durationMs)
                            }
                            if (state == Player.STATE_BUFFERING) {
                                isLoading.postValue(true)
                            }


                        }
                    })
                    startProgressUpdates(player)
                    player.play()
                    isLoading.postValue(false)
                }
                withContext(Dispatchers.IO){
                    if (videoItem.playlistId==null){
                        loadSuggestions(videoItem.videoId,visitorId,videoItem)
                    }
                }


            } catch (e: Exception) {
                isLoading.postValue(false)
                error.postValue(e.message)
            }
        }
    }
    override fun onCleared() {
        super.onCleared()
        stopProgressUpdates()
    }
    fun onUserInteraction() {
        showControls.postValue(true)

    }
    fun toggleControls() {
        val current = showControls.value ?: true
        if (current) {
            hideControls()
        } else {
            onUserInteraction()
        }
    }
    private fun hideControls() {
        showControls.postValue(false)
    }

    private fun loadSuggestions(videoId: String,visitorId: String,videoItem: VideoItem){
        val playerResponse= WatchNextBrowse.getSuggestions(videoId,null,visitorId,"2.20260324.05.00")
        println("parsingmmm")
        val result= parseWatchHtml(playerResponse,"watchInitial")
        println(result)
        suggestions.postValue(result.videos)
        continuation=result.continuation


        videoDetails.postValue(
            result.videoDetails?.copy(
                title = videoItem.title,
                channelName = videoItem.channelName
            )
        )

    }
    fun loadMoreSuggestions(visitorId: String?){
        val cont = continuation ?: return
        val visitor = visitorId ?: return

        if (isRequestInFlight) return   // ✅ instant check

        isRequestInFlight = true       //

    }
}