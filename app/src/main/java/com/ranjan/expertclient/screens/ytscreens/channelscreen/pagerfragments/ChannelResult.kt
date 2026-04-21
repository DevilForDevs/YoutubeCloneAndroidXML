package com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments

import com.ranjan.expertclient.models.VideoItem

data class ChannelResult(
    val continuation: String?,
    val videos: MutableList<VideoItem>
)