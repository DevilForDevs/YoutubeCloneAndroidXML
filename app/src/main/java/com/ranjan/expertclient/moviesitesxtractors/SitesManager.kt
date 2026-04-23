package com.ranjan.expertclient.moviesitesxtractors

import android.content.Context
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.sitesscreen.SiteItem

class SitesManager {
    fun getFeeds(siteItem: SiteItem,context: Context):MutableList<VideoItem>{
        if (siteItem.url.contains("mp4moviez")){
            val mp4moviez=Mp4moviez()
            return mp4moviez.getFeeds(siteItem.url,context)
        }
        return mutableListOf()

    }
    fun getPage(url: String,context: Context): MutableList<VideoItem> {
        if (url.contains("mp4moviez")){
            val mp4moviez=Mp4moviez()
            return mp4moviez.getFeeds(url,context)
        }
        return mutableListOf()

    }
}
