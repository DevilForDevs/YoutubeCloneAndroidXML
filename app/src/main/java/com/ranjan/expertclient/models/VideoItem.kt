package com.ranjan.expertclient.models

import com.ranjan.expertclient.moviesitesxtractors.SitesManager


data class VideoItem(
    val videoId: String,
    val title: String,
    val thumbnail: String?=null,
    val channelName: String?=null,
    val channelAvtar: String? = null,
    val channelUrl: String? = null,
    val views: String? = null,
    val duration: String? = null,
    val playlistId: String?=null,
    val shortsArray: MutableList<VideoItem>?=null,
    val publishedOn: String?=null,
    val yt: Boolean=true,
    val pageUrl: String?=null,
    val category: Boolean=true,
    val siteManager: SitesManager?=null,

)