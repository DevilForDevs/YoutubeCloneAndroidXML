package com.ranjan.expertclient.models

data class PlaylistMetaData(
    val playlistTitle: String,
    val channelId: String,
    val channelAvtar: String,
    val heroImage: String,
    val createdBy: String,
    val videosCount: String?,
    val views: String?
)