package com.ranjan.expertclient.moviesitesxtractors

import com.ranjan.expertclient.screens.sitesscreen.SiteItem

class SitesManager {
    fun getFeeds(siteItem: SiteItem){
        if (siteItem.url.contains("mp4moviez")){
            val mp4moviez=Mp4moviez()
            mp4moviez.getFeeds(siteItem.url)
        }

    }
}
