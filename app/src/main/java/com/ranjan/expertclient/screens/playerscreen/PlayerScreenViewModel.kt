package com.ranjan.expertclient.screens.playerscreen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import androidx.annotation.OptIn
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import com.ranjan.expertclient.screens.playerscreen.models.StreamItem
import com.ranjan.expertclient.screens.playerscreen.utils.WatchNextBrowse
import com.ranjan.expertclient.screens.playerscreen.utils.YtPlaylistBrowseFetcher
import com.ranjan.expertclient.screens.playerscreen.utils.getFmtList
import com.ranjan.expertclient.screens.playerscreen.utils.parseAdaptiveFormats
import com.ranjan.expertclient.screens.playerscreen.utils.parseWatchHtml
import com.ranjan.expertclient.screens.playerscreen.utils.prasePlaylist
import com.ranjan.expertclient.screens.playerscreen.widgets.models.VideoDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale


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
    var currentVideoId: String?=null
    private var _isSeeking = MutableLiveData(false)



    private var isRequestInFlight = false
    private  val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 15; CPH2665 Build/AP3A.240617.008; wv) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 " +
                "Chrome/143.0.7499.34 Mobile Safari/537.36"


    fun toggleFullScreen() {
        isFullScreen.value = !(isFullScreen.value ?: false)
    }

    fun fixInsets(view: View){
        ViewCompat.setOnApplyWindowInsetsListener(view) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
    }

    fun fixOrientation(activity: Activity){
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (isFullScreen.value){
            activity.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }else{
            activity.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            controller.show(WindowInsetsCompat.Type.systemBars())
        }

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

        viewModelScope.launch(Dispatchers.IO){
            try {
                currentVideoId=videoItem.videoId
                val streamingData = getStreamingData(
                    videoItem.playlistId?:videoItem.videoId,
                    visitorData = visitorId
                )
                adaptiveFormatsList= getFmtList(streamingData)
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
                if (videoItem.playlistId==null){
                    loadSuggestions(videoItem.videoId,visitorId,videoItem,streamingData.getJSONObject("playerResponse").getJSONObject("videoDetails"))
                }else{
                    loadPlaylist("",visitorId)
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

    fun seekTo(player: ExoPlayer, position: Long) {
        _isSeeking.value = true
        player.seekTo(position)
        _isSeeking.value = false
    }

    fun onProgressChanged(player: ExoPlayer, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            seekTo(player, progress.toLong())
        }
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

    private fun loadSuggestions(videoId: String,visitorId: String,videoItem: VideoItem,video_details: JSONObject){
        val playerResponse= WatchNextBrowse.getSuggestions(videoId,null,visitorId,"2.20260324.05.00")
        val result= parseWatchHtml(playerResponse,"watchInitial")
        suggestions.postValue(result.videos)
        continuation=result.continuation


        val view=video_details.getString("viewCount").toInt()
        val localizedViewCount = NumberFormat
            .getInstance(Locale.getDefault())
            .format(view)+" view • "+videoItem.publishedOn
        val keywordsArray = video_details.optJSONArray("keywords")

        val keywordsList = mutableListOf<String>()

        if (keywordsArray != null) {
            for (i in 0 until keywordsArray.length()) {
                val k = keywordsArray.optString(i)
                if (!k.isNullOrBlank()) {
                    keywordsList.add(k.trim())
                }
            }
        }

        videoDetails.postValue(
            result.videoDetails?.copy(
                title = video_details.getString("title"),
                channelName = videoItem.channelName,
                hashTags = keywordsList.joinToString(" ") {
                    "#${it.trim().replace(Regex("\\s+"), "_")}"
                },
                localLizedViewsandUploadedAgo = localizedViewCount,
                firstHasTag = keywordsList.firstOrNull()
                    ?.let { "#${it.replace(" ", "")}" } ?: ""
            )
        )

    }
    fun loadPlaylist(playlistId: String,visitorId: String){
        val response = YtPlaylistBrowseFetcher.fetch("browseId",playlistId, null,visitorId)
        val result= prasePlaylist(JSONObject(response),"playlist")
        suggestions.postValue(result.videos)
        continuation=result.continuation
    }
    fun loadMoreSuggestions(visitorId: String?){
        val cont = continuation ?: return
        val visitor = visitorId ?: return

        if (isRequestInFlight) return   // ✅ instant check

        isRequestInFlight = true

        viewModelScope.launch(Dispatchers.IO){
            println("fetchednewivid")
            /*val playerResponse= WatchNextBrowse.getSuggestions(currentVideoId?:"",null,visitorId,"2.20260324.05.00")
            val result= parseWatchHtml(playerResponse,"watchInitial")

            val oldVideos=suggestions.value
            if (oldVideos != null) {
                suggestions.postValue((oldVideos+result.videos) as MutableList<VideoItem>?)
            }
            continuation=result.continuation*/
            isRequestInFlight=false
        }

    }
}