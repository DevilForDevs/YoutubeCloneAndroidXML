package com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.playerscreen.utils.YtPlaylistBrowseFetcher
import com.ranjan.expertclient.screens.ytscreens.channelscreen.models.ChannelTab
import com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.videos.parseVideosTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class ViewModal: ViewModel() {
    private val videos = MutableLiveData<MutableList<VideoItem>>()
    val videosList: LiveData<MutableList<VideoItem>> get() = videos

    var isRequesting=false
    var continuation:String?=null
    val isLoading= MutableLiveData(false)

    companion object {
        private const val TAG = "ChannelVideosVM"
    }

    fun loadVideos(visitorId:String,channelTab: ChannelTab){

        if (isRequesting) return
        continuation = null
        videos.postValue(mutableListOf())
        isLoading.postValue(true)
        isRequesting=true
        viewModelScope.launch(Dispatchers.IO){
            try {
                val response= YtPlaylistBrowseFetcher.fetch("browseId",channelTab.browserId,channelTab.paras,visitorId)
                val json= JSONObject(response)
                val result= parseVideosTab(json, key = "initial", tabIndex = channelTab.tabIndex)
                videos.postValue(result.videos)
                continuation=result.continuation
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load channel videos", e)
            } finally {
                isRequesting=false
                isLoading.postValue(false)
            }
        }


    }
    fun loadContinuationItems(visitorId:String,channelTab: ChannelTab){
        if (isRequesting) return
        if (continuation==null)return
        isLoading.postValue(true)
        isRequesting=true
        viewModelScope.launch(Dispatchers.IO){
            try {
                val response= YtPlaylistBrowseFetcher.fetch("continuation",continuation?:"",channelTab.paras,visitorId)
                val json= JSONObject(response)
                val result= parseVideosTab(json, key = "continuation")

                val oldVideos=videos.value
                val channelAvtar= oldVideos?.firstOrNull()?.channelAvtar
                val newVideosWithChannelAvtar=result.videos.map { it.copy(channelAvtar = channelAvtar) }
                videos.postValue((oldVideos?.plus(newVideosWithChannelAvtar)
                    ?: result.videos.toMutableList()) as MutableList<VideoItem>?)
                continuation=result.continuation
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load continuation videos", e)
            } finally {
                isRequesting=false
                isLoading.postValue(false)
            }
        }
    }


}