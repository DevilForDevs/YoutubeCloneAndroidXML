package com.ranjan.expertclient.models

data class VideoNavState(
    val currentPage: VideoPage,
    val history: List<VideoPage> = emptyList(),
    val isLoading: Boolean = false
)
