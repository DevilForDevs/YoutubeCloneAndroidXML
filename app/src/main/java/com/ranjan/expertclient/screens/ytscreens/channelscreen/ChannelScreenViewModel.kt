package com.ranjan.expertclient.screens.ytscreens.channelscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ranjan.expertclient.screens.browserscreen.safeGet
import com.ranjan.expertclient.screens.playerscreen.utils.YtPlaylistBrowseFetcher
import com.ranjan.expertclient.screens.ytscreens.channelscreen.models.ChannelMetaData
import com.ranjan.expertclient.screens.ytscreens.channelscreen.models.ChannelTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ChannelScreenViewModel: ViewModel() {
    private val tabs = MutableLiveData<MutableList<ChannelTab>>(mutableListOf())
    val tabList: LiveData<MutableList<ChannelTab>> get() = tabs
    val channelMetaData = MutableLiveData<ChannelMetaData?>()
    val currentBrowseId = MutableLiveData<String?>()

    fun loadTabs(browseId: String, visitorId: String) {
        if (currentBrowseId.value==browseId) return
        currentBrowseId.value = browseId
        viewModelScope.launch(Dispatchers.IO) {

            val response = YtPlaylistBrowseFetcher.fetch("browseId", browseId, null, visitorId)
            val json = JSONObject(response)
            
            val channelTitle = safeGet(json, listOf(
                "header",
                "pageHeaderRenderer",
                "pageTitle",
            )) as? String

            val channelAvatar = safeGet(json, listOf(
                "header",
                "pageHeaderRenderer",
                "content",
                "pageHeaderViewModel",
                "image",
                "decoratedAvatarViewModel",
                "avatar",
                "avatarViewModel",
                "image",
                "sources",
                -1,
                "url",
            )) as? String

            val subscriberCount = safeGet(json, listOf(
                "header",
                "pageHeaderRenderer",
                "content",
                "pageHeaderViewModel",
                "metadata",
                "contentMetadataViewModel",
                "metadataRows", 1,
                "metadataParts", 0,
                "text",
                "content",
            )) as? String

            val totalVideos = safeGet(json, listOf(
                "header",
                "pageHeaderRenderer",
                "content",
                "pageHeaderViewModel",
                "metadata",
                "contentMetadataViewModel",
                "metadataRows", 1,
                "metadataParts", 1,
                "text",
                "content",
            )) as? String

            val posterImage = safeGet(json, listOf(
                "header",
                "pageHeaderRenderer",
                "content",
                "pageHeaderViewModel",
                "banner",
                "imageBannerViewModel",
                "image",
                "sources",
                -1,
                "url",
            )) as? String

            val tabsJson = safeGet(json, listOf(
                "contents",
                "twoColumnBrowseResultsRenderer",
                "tabs"
            )) as? JSONArray
            
            val _tabs = mutableListOf<ChannelTab>()

            if (tabsJson != null) {
                for (i in 0 until tabsJson.length()) {
                    val item = tabsJson.getJSONObject(i)
                    val paras = safeGet(item, listOf(
                        "tabRenderer",
                        "endpoint", "browseEndpoint", "params"
                    )) as? String
                    
                    if (paras == null) {
                        continue
                    }
                    
                    val tabBrowseId = safeGet(item, listOf(
                        "tabRenderer",
                        "endpoint", "browseEndpoint", "browseId"
                    )) as? String ?: ""
                    
                    val tabTitle = safeGet(item, listOf(
                        "tabRenderer",
                        "title"
                    )) as? String ?: ""
                    
                    _tabs.add(ChannelTab(
                        tabIndex = i,
                        paras = paras,
                        browserId = tabBrowseId,
                        title = tabTitle
                    ))
                }
            }


            this@ChannelScreenViewModel.tabs.postValue(mutableListOf())
            this@ChannelScreenViewModel.tabs.postValue(_tabs)

            channelMetaData.postValue(ChannelMetaData(
                title = channelTitle,
                channelAvatar = channelAvatar,
                subscriberCount = subscriberCount,
                totalVideos = totalVideos,
                posterImage = posterImage,
            ))
        }
    }
}
