package com.ranjan.expertclient.screens.playerscreen.models

data class DownloadItem(
    val resolution: String,
    val downloaded: Long,
    val total: Long,
    val speed: String,
    val isDownloading: Boolean=true,
    val fileName: String,
    val fileUrl: String,
    val isPlaying: Boolean=false,
    val isFinished: Boolean=false,
    val status: String

)
