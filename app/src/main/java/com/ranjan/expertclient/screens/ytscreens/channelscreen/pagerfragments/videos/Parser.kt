package com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.videos

import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.browserscreen.safeGet
import com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.ChannelResult
import org.json.JSONArray
import org.json.JSONObject

fun extractChannelVideo(
    item: JSONObject,
    channelTitle: String?,
    channelAvatar: String?
): VideoItem {
    val vr = (safeGet(
        item,
        listOf("richItemRenderer", "content", "videoRenderer"),
        JSONObject()
    ) as? JSONObject) ?: JSONObject()

    val views =
        (safeGet(vr, listOf("shortViewCountText", "simpleText"), null) as? String)
            ?: (safeGet(vr, listOf("viewCountText", "simpleText"), null) as? String)

    val videoId = safeGet(vr, listOf("videoId"), "") as? String ?: ""
    val title = safeGet(vr, listOf("title", "runs", 0, "text"), "") as? String ?: ""
    val duration = safeGet(vr, listOf("lengthText", "simpleText"), "") as? String ?: ""
    val publishedOn = safeGet(vr, listOf("publishedTimeText", "simpleText"), "") as? String ?: ""

    return VideoItem(
        videoId = videoId,
        title = title,
        shortsArray = null,
        channelName = channelTitle,
        channelAvtar = channelAvatar,
        channelUrl = null,
        views = views,
        duration = duration,
        publishedOn = publishedOn
    )
}


fun parseVideosTab(
    data: JSONObject,
    tabIndex: Int = 0,
    key: String = "both"
): ChannelResult {
    val videos = mutableListOf<VideoItem>()
    var continuationToken: String? = null

    val channelTitle = safeGet(
        data,
        listOf("header", "pageHeaderRenderer", "pageTitle"),
        null
    ) as? String

    val channelAvatar = safeGet(
        data,
        listOf(
            "header",
            "pageHeaderRenderer",
            "content",
            "pageHeaderViewModel",
            "image",
            "decoratedAvatarViewModel",
            "avatar",
            "avatarViewModel",
            "image",
            "sources",
            -1,
            "url"
        ),
        null
    ) as? String

    fun processItems(items: JSONArray) {
        for (i in 0 until items.length()) {
            val item = items.optJSONObject(i) ?: continue

            if (item.has("richItemRenderer")) {
                videos.add(extractChannelVideo(item, channelTitle, channelAvatar))
            }

            if (item.has("continuationItemRenderer")) {
                continuationToken = safeGet(
                    item,
                    listOf(
                        "continuationItemRenderer",
                        "continuationEndpoint",
                        "continuationCommand",
                        "token"
                    ),
                    continuationToken
                ) as? String
            }
        }
    }

    if (key == "initial" || key == "both") {
        val gridItems = (safeGet(
            data,
            listOf(
                "contents",
                "twoColumnBrowseResultsRenderer",
                "tabs",
                tabIndex,
                "tabRenderer",
                "content",
                "richGridRenderer",
                "contents"
            ),
            JSONArray()
        ) as? JSONArray) ?: JSONArray()
        processItems(gridItems)
    }

    if (key == "continuation" || key == "both") {
        val continuationItems = (safeGet(
            data,
            listOf(
                "onResponseReceivedActions",
                0,
                "appendContinuationItemsAction",
                "continuationItems"
            ),
            JSONArray()
        ) as? JSONArray) ?: JSONArray()

        processItems(continuationItems)
    }

    return ChannelResult(
        continuation = continuationToken,
        videos = videos
    )
}
