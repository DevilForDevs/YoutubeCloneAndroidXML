package com.ranjan.expertclient.screens.ytscreens.searchscreen.parsers

import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.browserscreen.parsers.getContentArray
import com.ranjan.expertclient.screens.browserscreen.safeGet
import org.json.JSONArray
import org.json.JSONObject


fun parseShorts(shorts: JSONArray): MutableList<VideoItem> {
    val shortslist=mutableListOf<VideoItem>()
    for (i in 0 until shorts.length()) {
        val item=shorts.getJSONObject(i).getJSONObject("shortsLockupViewModel")

        val videoId=safeGet(item,listOf(
            "onTap",
            "innertubeCommand",
            "reelWatchEndpoint",
            "videoId"
        ))
        val title=safeGet(item,listOf(
            "overlayMetadata",
            "primaryText",
            "content"
        ))

        val views=safeGet(item,listOf(
            "overlayMetadata",
            "secondaryText",
            "content"
        ))
        if (videoId==null){
            continue
        }
        shortslist.add(VideoItem(
            videoId as String,
            title as String,
            views = views as String?
        ))
    }
    return shortslist
}

fun createVideoTree(item: JSONObject): VideoItem {
    val videoId=item.getString("videoId")
    val channelId= safeGet(item,listOf(
        "avatar",
        "decoratedAvatarViewModel", "rendererContext", "commandContext", "onTap",
        "innertubeCommand", "browseEndpoint", "browseId"
    ))
    val channelName= safeGet(item,listOf(
        "avatar",
        "decoratedAvatarViewModel", "rendererContext", "commandContext", "onTap",
        "innertubeCommand", "browseEndpoint", "canonicalBaseUrl"
    ),"No Title") as String


    val title=safeGet(item,
        listOf(
            "title", "runs", 0, "text"
        )
    )
    val duration=safeGet(item,listOf(
        "lengthText", "simpleText"
    ))
    val views=safeGet(item,listOf(
        "shortViewCountText", "simpleText"
    ))
    val channelAvatar=safeGet(item,listOf(
        "avatar",
        "decoratedAvatarViewModel",
        "avatar",
        "avatarViewModel",
        "image",
        "sources",
        0,
        "url"
    ))
    val publishedOn=safeGet(item,listOf(
        "publishedTimeText",
        "simpleText"
    ))

    return VideoItem(
        videoId,
        title as String,
        duration = duration as String?,
        channelAvtar = channelAvatar as String?,
        views = views as String?,
        publishedOn = publishedOn as String?,
        channelUrl = channelId as String?,
        channelName = channelName.replace("/@", "")
    )

}
fun parseSearchYt(root: JSONObject, flags: String): Pair<MutableList<VideoItem>, String> {
    val contentsArray = getContentArray(root, flags)
    val videoItems = mutableListOf<VideoItem>()
    var continuationToken = ""

    for (i in 0 until contentsArray.length()) {

        val item = contentsArray.getJSONObject(i)

        if (item.has("videoRenderer")) {
            videoItems.add(createVideoTree(item.getJSONObject("videoRenderer")))
        }

        if (item.has("shelfRenderer")) {

            val secondRow = safeGet(
                item,
                listOf("shelfRenderer", "content", "verticalListRenderer", "items")
            ) as JSONArray

            for (j in 0 until secondRow.length()) {

                val item2 = secondRow.getJSONObject(j) // ✅ FIXED

                if (item2.has("videoRenderer")) {
                    videoItems.add(createVideoTree(item2.getJSONObject("videoRenderer")))
                }

                if (item2.has("gridShelfViewModel")) {
                    val shortsArray = safeGet(
                        item2,
                        listOf("gridShelfViewModel", "contents")
                    ) as JSONArray

                    val shortsList = parseShorts(shortsArray)

                    if (shortsList.isNotEmpty()) {
                        videoItems.add(
                            VideoItem(
                                videoId = shortsList[0].videoId,
                                title = "Shorts",
                                shortsArray = shortsList
                            )
                        )
                    }
                }
            }
        }

        if (item.has("gridShelfViewModel")) {
            val shortsArray = safeGet(
                item,
                listOf("gridShelfViewModel", "contents")
            ) as JSONArray

            val shortsList = parseShorts(shortsArray)

            if (shortsList.isNotEmpty()) {
                videoItems.add(
                    VideoItem(
                        videoId = shortsList[0].videoId,
                        title = "Shorts",
                        shortsArray = shortsList
                    )
                )
            }
        }
    }

    // ✅ Continuation handling
    continuationToken = when (flags) {

        "ytsearch" -> safeGet(
            root, listOf(
                "contents",
                "twoColumnSearchResultsRenderer",
                "primaryContents",
                "sectionListRenderer",
                "contents",
                1,
                "continuationItemRenderer",
                "continuationEndpoint",
                "continuationCommand",
                "token"
            )
        )?.toString() ?: ""

        "ytsearch_continuation" -> safeGet(
            root, listOf(
                "onResponseReceivedCommands",
                0,
                "appendContinuationItemsAction",
                "continuationItems",
                1,
                "continuationItemRenderer",
                "continuationEndpoint",
                "continuationCommand",
                "token"
            )
        )?.toString() ?: ""

        else -> ""
    }

    return Pair(videoItems, continuationToken) // ✅ FIXED
}