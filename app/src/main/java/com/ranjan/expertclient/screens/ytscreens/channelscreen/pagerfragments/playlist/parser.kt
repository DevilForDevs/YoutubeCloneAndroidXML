package com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.playlist

import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.browserscreen.safeGet
import org.json.JSONArray
import com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.ChannelResult



fun extractPlaylist(lockup: Any?): VideoItem? {
    val playlistId =
        safeGet(lockup, listOf("contentId"))
            ?: safeGet(
                lockup,
                listOf(
                    "metadata",
                    "lockupMetadataViewModel",
                    "metadata",
                    "contentMetadataViewModel",
                    "metadataRows",
                    0,
                    "metadataParts",
                    0,
                    "text",
                    "commandRuns",
                    0,
                    "onTap",
                    "innertubeCommand",
                    "browseEndpoint",
                    "browseId",
                ),
            )
    val playlistIdString = playlistId as? String ?: return null

    val title =
        safeGet(
            lockup,
            listOf(
                "metadata",
                "lockupMetadataViewModel",
                "title",
                "content",
            ),
        ) as? String ?: return null

    val videoId =
        safeGet(
            lockup,
            listOf(
                "itemPlayback",
                "inlinePlayerData",
                "onSelect",
                "innertubeCommand",
                "watchEndpoint",
                "videoId",
            ),
        ) as? String ?: return null

    val views =
        safeGet(
            lockup,
            listOf(
                "contentImage",
                "collectionThumbnailViewModel",
                "primaryThumbnail",
                "thumbnailViewModel",
                "overlays",
                0,
                "thumbnailOverlayBadgeViewModel",
                "thumbnailBadges",
                0,
                "thumbnailBadgeViewModel",
                "text",
            ),
        ) as? String

    return VideoItem(
        title = title,
        videoId = playlistIdString,
        views = views,
        playlistId = videoId,
    )
}

fun extractPlaylistsFromBrowse(
    json: Any?,
    tabIndex: Int?,
): ChannelResult {
    val playlists = mutableListOf<VideoItem>()
    var continuationToken: String? = null
    if (tabIndex==null){
       val contentsArray=safeGet(json,listOf(
           "onResponseReceivedActions",
           0,
           "appendContinuationItemsAction",
           "continuationItems",
       ), JSONArray()) as JSONArray

        for (i in 0 until contentsArray.length()) {
            val item=contentsArray.getJSONObject(i)

            if (item.has("continuationItemRenderer")){
                continuationToken = safeGet(
                    item,
                    listOf(
                        "continuationItemRenderer",
                        "continuationEndpoint",
                        "continuationCommand",
                        "token",
                    ),
                ) as? String
            }
            if (item.has("lockupViewModel")){
                val lockup = safeGet(item, listOf("lockupViewModel"))
                if (lockup == null) continue
                val pl = extractPlaylist(lockup)
                if (pl != null) playlists.add(pl)
            }
        }
    }else{
        val contentsArray=safeGet(json,listOf(
            "contents",
            "twoColumnBrowseResultsRenderer",
            "tabs",
            tabIndex,
            "tabRenderer",
            "content",
            "sectionListRenderer",
            "contents",
            0,
            "itemSectionRenderer",
            "contents",
            0,
            "gridRenderer",
            "items",
        ), JSONArray()) as JSONArray

        for (i in 0 until contentsArray.length()) {
            val item=contentsArray.getJSONObject(i)

            if (item.has("continuationItemRenderer")){
                continuationToken = safeGet(
                    item,
                    listOf(
                        "continuationItemRenderer",
                        "continuationEndpoint",
                        "continuationCommand",
                        "token",
                    ),
                ) as? String
            }
            if (item.has("lockupViewModel")){
                val lockup = safeGet(item, listOf("lockupViewModel"))
                if (lockup == null) continue
                val pl = extractPlaylist(lockup)
                if (pl != null) playlists.add(pl)
            }
        }
    }


    return ChannelResult(continuation = continuationToken, videos = playlists)
}

