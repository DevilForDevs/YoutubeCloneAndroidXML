package com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.shorts

import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.browserscreen.safeGet
import com.ranjan.expertclient.screens.ytscreens.channelscreen.pagerfragments.ChannelResult
import org.json.JSONArray
import org.json.JSONObject

fun extractShorts(
    data: JSONObject,
    tabIndex: Int,
    key: String = "auto" // "initial" | "continuation" | "both" | "auto"
): ChannelResult {
    val items = mutableListOf<VideoItem>()
    var continuationToken: String? = null

    fun processRawItems(rawItems: JSONArray) {
        for (i in 0 until rawItems.length()) {
            val item = rawItems.optJSONObject(i) ?: continue

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
                continue
            }

            val lockup = safeGet(
                item,
                listOf("richItemRenderer", "content", "shortsLockupViewModel"),
                null
            ) as? JSONObject ?: continue

            val videoId = safeGet(
                lockup,
                listOf("onTap", "innertubeCommand", "reelWatchEndpoint", "videoId"),
                null
            ) as? String ?: continue

            val title = safeGet(
                lockup,
                listOf("overlayMetadata", "primaryText", "content"),
                ""
            ) as? String ?: ""

            val views = safeGet(
                lockup,
                listOf("overlayMetadata", "secondaryText", "content"),
                ""
            ) as? String ?: ""

            items.add(
                VideoItem(
                    videoId = videoId,
                    title = title,
                    views = views
                )
            )
        }
    }

    val mode = when (key) {
        "initial", "continuation", "both" -> key
        else -> if (data.has("onResponseReceivedActions")) "continuation" else "initial"
    }

    if (mode == "initial" || mode == "both") {
        val initialItems = (safeGet(
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

        processRawItems(initialItems)
        if (mode == "initial") {
            return ChannelResult(videos = items, continuation = continuationToken)
        }
    }

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

    processRawItems(continuationItems)

    return ChannelResult(videos = items, continuation = continuationToken)
}
