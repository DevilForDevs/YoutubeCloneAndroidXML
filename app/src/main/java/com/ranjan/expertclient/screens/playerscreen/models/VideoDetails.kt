package com.ranjan.expertclient.screens.playerscreen.models

data class VideoDetails(
    val title: String,
    val likes: String,
    val dislikes: String?,
    val channelBigThumb: String,
    val commentsCount: String,
    val localLizedViewsandUploadedAgo: String,
    val subscriberCount: String,
    val firstHasTag: String,
    val hashTags: String,
    val channelName: String?,
    val channelUrl: String?
)