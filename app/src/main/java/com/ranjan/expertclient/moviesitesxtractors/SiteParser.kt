package com.ranjan.expertclient.moviesitesxtractors

import com.ranjan.expertclient.models.PraseResult
import com.ranjan.expertclient.screens.playerscreen.models.StreamItem

interface SiteParser {
    var localSchemaFolder: String?
    val siteTitle: String
    fun getFeeds(url: String, schemasFolder: String): PraseResult
    fun getPage(url: String): PraseResult
    fun getVideoUrls(url: String): MutableList<StreamItem>
}