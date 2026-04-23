package com.ranjan.expertclient.screens.moviessitesclient.moviesfeeds


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.moviesitesxtractors.SitesManager
import com.ranjan.expertclient.screens.sitesscreen.SiteItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MoviesScreenViewModel : ViewModel() {
    private val _moviesList = MutableLiveData<MutableList<VideoItem>>()
    val moviesList: LiveData<MutableList<VideoItem>> = _moviesList
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    val sitesManager= SitesManager()
    fun getFeeds(siteItem: SiteItem){
        _loading.value=true
        viewModelScope.launch(Dispatchers.IO){
            sitesManager.getFeeds(siteItem)
        }




    }
}