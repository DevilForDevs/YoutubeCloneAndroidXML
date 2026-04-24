package com.ranjan.expertclient.screens.playerscreen

import androidx.annotation.OptIn
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.ranjan.expertclient.apiendpoints.getStreamingData
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.playerscreen.controllers.PlayerManager
import com.ranjan.expertclient.screens.playerscreen.models.StreamItem
import com.ranjan.expertclient.screens.playerscreen.utils.YtHelpers
import com.ranjan.expertclient.screens.playerscreen.utils.getFmtList
import com.ranjan.expertclient.screens.playerscreen.models.VideoDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


@Suppress("unused")
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
    var _isSeeking = MutableLiveData(false)
    val current_playlistId = MutableLiveData<String?>()
    val currentResolution = MutableLiveData<String?>()
    val systemVolume = MutableLiveData<Int?>()
    val userVolume = MutableLiveData<Int?>()
    val systemBrightness = MutableLiveData<Int?>()
    val userBrightness = MutableLiveData<Int?>()

    fun rememberSystemVolume(volume: Int) {
        if (systemVolume.value == null) {
            systemVolume.value = volume
        }
    }

    fun rememberUserVolume(volume: Int) {
        userVolume.value = volume
    }

    fun rememberSystemBrightness(brightness: Int) {
        if (systemBrightness.value == null) {
            systemBrightness.value = brightness
        }
    }

    fun rememberUserBrightness(brightness: Int) {
        userBrightness.value = brightness
    }

    fun preferredVolume(): Int? {
        return userVolume.value ?: systemVolume.value
    }

    fun preferredBrightness(): Int? {
        return userBrightness.value ?: systemBrightness.value
    }

    private var isRequestInFlight = false
    val loadingMore = MutableLiveData(false)

    
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

    fun stopProgressUpdates() {
        progressJob?.cancel()
    }

    fun resetSimplePlaybackState() {
        stopProgressUpdates()
        isLoading.postValue(true)
        isPaused.postValue(false)
        error.postValue(null)
        totalDuration.postValue(null)
        durationProgress.postValue(null)
        videoDetails.postValue(null)
        currentResolution.postValue(null)
        _isSeeking.postValue(false)
        showControls.postValue(true)
        suggestions.postValue(mutableListOf())
        continuation = null
        currentVideoId = null
        current_playlistId.postValue(null)
        adaptiveFormatsList = emptyList()
    }

    @OptIn(UnstableApi::class)
    fun loadVideoAndGetFormats(
        videoItem: VideoItem,
        visitorId: String
    ): JSONObject {

        currentVideoId = videoItem.videoId

        val streamingData = getStreamingData(
            videoItem.playlistId?:videoItem.videoId,
            visitorData = visitorId
        )

        return streamingData
    }

    @OptIn(UnstableApi::class)
    fun loadVideo(
        videoItem: VideoItem,
        visitorId: String,
        playerManager: PlayerManager
    ) {
        isPaused.value=true
        isLoading.value = true
        error.value = null
        totalDuration.postValue(0L)
        durationProgress.postValue(0L)

        viewModelScope.launch {
            try {
                val playerResponse = withContext(Dispatchers.IO) {
                    loadVideoAndGetFormats(videoItem, visitorId)
                }

                val formats = getFmtList(playerResponse)
                adaptiveFormatsList = formats

                playerManager.play(formats) // already on Main thread ✅
                withContext(Dispatchers.IO){
                    loadSuggestions(
                         visitorId, videoItem,
                        video_details =playerResponse.getJSONObject("playerResponse").getJSONObject("videoDetails"),
                    )
                }
            }catch (e: Exception){
                println(e.printStackTrace())
            }
        }
    }



    fun getResolutionList(): List<String> {
        return adaptiveFormatsList
            .filter { it.height != null }
            .map { "${it.height}p" }
            .distinct()
            .sortedByDescending { it.dropLast(1).toIntOrNull() ?: 0 }
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressUpdates()
    }

    fun loadSuggestions(visitorId: String, videoItem: VideoItem, video_details: JSONObject) {
        val watchData = YtHelpers.getInitialWatchData(videoItem, visitorId, video_details)
        val result = watchData.initialData
        val localizedViewCount = watchData.localizedViewCount
        val keywordsList = watchData.keywords

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


        if (videoItem.playlistId == null) {
            suggestions.postValue(result.videos)
            continuation = result.continuation
        } else {
            val cp = current_playlistId.value
            if (cp != videoItem.playlistId) {
                /*misleading zone ,,by all parsers output videoId is acutaly palylistid and palylistid is acutaly videoId*/
                /*most conents are videoItem with videoId field a videoItem(regular) comes leading to palyist VideoItem(that already might have same video) this will collaspe diffutils*/
                /*using double filters in difutils will might make the diffutils slow*/
                current_playlistId.postValue(videoItem.videoId)
                loadPlaylist(videoItem.videoId, visitorId)
            }

        }

    }
    fun loadPlaylist(playlistId: String, visitorId: String) {
        continuation = null
        val result = YtHelpers.getPlaylist(playlistId, visitorId)
        val channelPhoto = result.metaData?.channelAvtar
        suggestions.postValue(result.videos.map {
            it.copy(
                channelAvtar = channelPhoto,
                videoId = playlistId
            )
        }.toMutableList())
        continuation = result.continuation
    }
    fun loadMoreSuggestions(visitorId: String?) {
        val cont = continuation ?: return
        val visitor = visitorId ?: return

        if (isRequestInFlight) return

        isRequestInFlight = true
        loadingMore.postValue(true)
        val currentitem = suggestions.value?.find { it.videoId == currentVideoId }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resultVideos = if (currentitem?.playlistId == null) {
                    val result = YtHelpers.getNextSuggestions(currentVideoId ?: "", cont, visitor)
                    continuation = result.continuation
                    result.videos
                } else {
                    val result = YtHelpers.getPlaylistContinuation(cont, visitor)
                    continuation = result.continuation
                    result.videos
                }

                suggestions.value?.let { oldVideos ->
                    suggestions.postValue((oldVideos + resultVideos).toMutableList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isRequestInFlight = false
                loadingMore.postValue(false)
            }
        }
    }


}