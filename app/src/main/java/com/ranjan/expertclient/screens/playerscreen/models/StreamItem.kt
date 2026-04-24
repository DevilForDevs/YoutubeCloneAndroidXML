package com.ranjan.expertclient.screens.playerscreen.models


data class StreamItem(
    val itag: Int,
    val url: String,
    val mimeType: String,
    val height: Int?,
    val bitrate: Int,
    var isSelected: Boolean = false,
    val resolutionString: String?=null,
    val size: String?=null
)