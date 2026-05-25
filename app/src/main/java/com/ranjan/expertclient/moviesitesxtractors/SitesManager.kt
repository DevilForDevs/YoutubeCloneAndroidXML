package com.ranjan.expertclient.moviesitesxtractors

import com.ranjan.expertclient.models.PraseResult
import com.ranjan.expertclient.moviesitesxtractors.sitehandlers.Mp4moviez
import com.ranjan.expertclient.screens.playerscreen.models.StreamItem
import com.ranjan.expertclient.screens.sitesscreen.SiteItem
import java.io.File

class SitesManager {

    // 👇 Only place you ever touch when adding a site
    private val parsers: List<SiteParser> = listOf<SiteParser>(
        Mp4moviez()
    )
     var currentSite:SiteItem?=null
     var siteFolder: String?=null

     var currentParser: SiteParser? = null


    private val empty = PraseResult(items = mutableListOf(), nextPageUrl = null)

    fun getFeeds(siteItem: SiteItem, siteFolder: String): PraseResult {
        currentSite=siteItem
        this@SitesManager.siteFolder =siteFolder
        val schemasFolder= File(siteFolder, "schemas")
        if (!schemasFolder.exists()){
            schemasFolder.mkdir()
        }
        currentParser = parsers.firstOrNull { siteItem.title.contains(it.siteTitle) }
        return currentParser?.getFeeds(siteItem.url, schemasFolder.absolutePath) ?: empty
    }

    fun getPage(url:String): PraseResult {
       return currentParser?.getPage(url) ?: empty
    }

    fun getVideoUrls(url:String): MutableList<StreamItem> {
       return currentParser?.getVideoUrls(url) ?: mutableListOf()
    }
}
