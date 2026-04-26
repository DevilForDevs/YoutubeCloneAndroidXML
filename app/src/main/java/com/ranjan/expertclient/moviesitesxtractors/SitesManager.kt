package com.ranjan.expertclient.moviesitesxtractors

import android.content.Context
import com.ranjan.expertclient.models.PraseResult
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.playerscreen.models.StreamItem
import com.ranjan.expertclient.screens.sitesscreen.SiteItem

class SitesManager {
    fun getFeeds(siteItem: SiteItem,context: Context): PraseResult {
        if (siteItem.url.contains("mp4moviez")){
            val mp4moviez=Mp4moviez()
            return mp4moviez.getFeeds(siteItem.url,context,siteItem.title)
        }
        return PraseResult(
            items = mutableListOf(),
            nextPageUrl = null
        )

    }
    fun getPage(url: String,context: Context,folder: String): PraseResult {
        if (url.contains("mp4moviez")){
            val mp4moviez=Mp4moviez()
            return mp4moviez.getPage(url,context,folder)
        }
        return PraseResult(
            items = mutableListOf(),
            nextPageUrl = null
        )

    }

    fun getVideoUrls(url: String,context: Context,folder: String): MutableList<StreamItem> {
        if (url.contains("mp4moviez")){
            val mp4moviez=Mp4moviez()
            return mp4moviez.getVideoUrls(url,context,folder)
        }
        return mutableListOf()
    }
}
