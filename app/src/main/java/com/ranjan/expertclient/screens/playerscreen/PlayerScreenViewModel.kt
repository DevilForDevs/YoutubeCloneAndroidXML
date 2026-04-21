package com.ranjan.expertclient.screens.playerscreen

import androidx.annotation.OptIn
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.ranjan.expertclient.apiendpoints.getStreamingData
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.playerscreen.controllers.PlayerManager
import com.ranjan.expertclient.screens.playerscreen.models.StreamItem
import com.ranjan.expertclient.screens.playerscreen.utils.WatchNextBrowse
import com.ranjan.expertclient.screens.playerscreen.utils.YtPlaylistBrowseFetcher
import com.ranjan.expertclient.screens.playerscreen.utils.getFmtList
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
    var _isSeeking = MutableLiveData(false)
    val current_playlistId = MutableLiveData<String?>()
    val currentResolution = MutableLiveData<String?>()

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

    fun loadSuggestions(visitorId: String,videoItem: VideoItem,video_details: JSONObject){
        val playerResponse= WatchNextBrowse.getSuggestions(videoItem.playlistId?:videoItem.videoId,null,visitorId,"2.20260324.05.00")
        val result= parseWatchHtml(playerResponse,"watchInitial")
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


        if (videoItem.playlistId == null) {
            suggestions.postValue(result.videos)
            continuation = result.continuation
        } else {
            val cp = current_playlistId.value
            if (cp != videoItem.playlistId) {
                /*misleading zone ,,by all parsers output videoId is acutaly palylistid and palylistid is acutaly videoId*/
                current_playlistId.postValue(videoItem.videoId)
                loadPlaylist(videoItem.videoId, visitorId)
            }

        }

    }
    fun loadPlaylist(playlistId: String,visitorId: String){
        val response = YtPlaylistBrowseFetcher.fetch("browseId", "VL$playlistId", null,visitorId)
        continuation=null
        val result= prasePlaylist(JSONObject(response),"playlist")
        val channelPhoto = result.metaData?.channelAvtar
        suggestions.postValue(result.videos.map { it.copy(channelAvtar = channelPhoto, videoId = playlistId) } as MutableList<VideoItem>?)
        continuation=result.continuation


    }
    fun loadMoreSuggestions(visitorId: String?){
        val cont = continuation ?: return
        val visitor = visitorId ?: return

        if (isRequestInFlight) return   // ✅ instant check

        isRequestInFlight = true
        loadingMore.postValue(true)
        val currentitem=suggestions.value?.find { it.videoId == currentVideoId }


        viewModelScope.launch(Dispatchers.IO){
            if (currentitem?.playlistId ==null){
                val playerResponse= WatchNextBrowse.getSuggestions(currentVideoId?:"",cont,visitor,"2.20260324.05.00")
                val result= parseWatchHtml(playerResponse,"watchContinuation")
                val oldVideos=suggestions.value
                if (oldVideos != null) {
                    suggestions.postValue((oldVideos+result.videos) as MutableList<VideoItem>?)
                }
                continuation=result.continuation
                isRequestInFlight=false
                loadingMore.postValue(false)
            }else{
                val response = YtPlaylistBrowseFetcher.fetch("continuation",cont, null,visitorId)
                val result= prasePlaylist(JSONObject(response),"playlist_continuation")
                val oldVideos=suggestions.value
                if (oldVideos != null) {
                    suggestions.postValue((oldVideos+result.videos) as MutableList<VideoItem>?)
                }
                continuation=result.continuation
                isRequestInFlight=false
                loadingMore.postValue(false)
            }

        }

    }


}