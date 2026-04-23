package com.ranjan.expertclient.screens.commanscreens.feeds_screen


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.sitesscreen.SiteItem

class FeedsScreenViewModel : ViewModel() {
    private val _feedsList = MutableLiveData<MutableList<VideoItem>>()
    val feedsList: LiveData<MutableList<VideoItem>> = _feedsList
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    fun loadingFeeds(siteItem: SiteItem){
       _loading.postValue(true)
    }
}