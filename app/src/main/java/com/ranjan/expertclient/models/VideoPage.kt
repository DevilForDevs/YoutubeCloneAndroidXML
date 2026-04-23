package com.ranjan.expertclient.models

data class VideoPage(
    val items: List<VideoItem>,
    val title: String? = null,
    val sourceId: String? = null // playlistId / category / pageUrl
)