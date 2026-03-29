package com.ranjan.expertclient.screens.playerscreen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ranjan.expertclient.models.VideoItem

class SharedVideoViewModel : ViewModel() {
    val selectedVideo = MutableLiveData<VideoItem>()
}