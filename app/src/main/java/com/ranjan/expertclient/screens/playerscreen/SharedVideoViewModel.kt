package com.ranjan.expertclient.screens.playerscreen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.sitesscreen.SiteItem

class SharedVideoViewModel : ViewModel() {
    val selectedVideo = MutableLiveData<VideoItem>()
    val selectedSite= MutableLiveData<SiteItem>()
}