package com.ranjan.expertclient.screens.playerscreen.widgets.models

import com.ranjan.expertclient.models.VideoItem

data class InitialDataModel(
    val videoDetails: VideoDetails?,
    val continuation: String?,
    val videos: MutableList<VideoItem>
)