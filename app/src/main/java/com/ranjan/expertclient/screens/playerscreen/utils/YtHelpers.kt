package com.ranjan.expertclient.screens.playerscreen.utils

import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.playerscreen.models.InitialDataModel
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

object YtHelpers {

    fun getInitialWatchData(
        videoItem: VideoItem,
        visitorId: String,
        videoDetailsJson: JSONObject
    ): WatchDataResult {
        val playerResponse = WatchNextBrowse.getSuggestions(
            videoItem.playlistId ?: videoItem.videoId,
            null,
            visitorId,
            "2.20260324.05.00"
        )
        val result = parseWatchHtml(playerResponse, "watchInitial")
        val view = videoDetailsJson.optString("viewCount", "0").toIntOrNull() ?: 0
        val localizedViewCount = NumberFormat
            .getInstance(Locale.getDefault())
            .format(view) + " view • " + videoItem.publishedOn

        val keywordsArray = videoDetailsJson.optJSONArray("keywords")
        val keywordsList = mutableListOf<String>()

        if (keywordsArray != null) {
            for (i in 0 until keywordsArray.length()) {
                val k = keywordsArray.optString(i)
                if (!k.isNullOrBlank()) {
                    keywordsList.add(k.trim())
                }
            }
        }
        return WatchDataResult(result, localizedViewCount, keywordsList)
    }

    fun getNextSuggestions(
        videoId: String,
        continuation: String,
        visitorId: String
    ): InitialDataModel {
        val playerResponse = WatchNextBrowse.getSuggestions(
            videoId,
            continuation,
            visitorId,
            "2.20260324.05.00"
        )
        return parseWatchHtml(playerResponse, "watchContinuation")
    }

    fun getPlaylist(
        playlistId: String,
        visitorId: String
    ): PlaylistInfo {
        val response = YtPlaylistBrowseFetcher.fetch("browseId", "VL$playlistId", null, visitorId)
        return prasePlaylist(JSONObject(response), "playlist")
    }

    fun getPlaylistContinuation(
        continuationToken: String,
        visitorId: String
    ): PlaylistInfo {
        val response = YtPlaylistBrowseFetcher.fetch("continuation", continuationToken, null, visitorId)
        return prasePlaylist(JSONObject(response), "playlist_continuation")
    }
}

data class WatchDataResult(
    val initialData: InitialDataModel,
    val localizedViewCount: String,
    val keywords: List<String>
)
