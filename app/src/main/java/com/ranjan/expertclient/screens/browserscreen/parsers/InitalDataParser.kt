package com.ranjan.expertclient.screens.browserscreen.parsers

import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.browserscreen.safeGet
import org.json.JSONArray
import org.json.JSONObject


fun getContentArray(resoponseContext: JSONObject,flags: String): JSONArray{

    if (flags=="ytsearch"){
        return safeGet(
            resoponseContext,
            listOf(
                "contents",
                "twoColumnSearchResultsRenderer",
                "primaryContents",
                "sectionListRenderer",
                "contents",
                0,
                "itemSectionRenderer",
                "contents"
            ),
            JSONArray()
        ) as JSONArray
    }

    if (flags=="ytsearch_continuation"){
        return safeGet(
            resoponseContext,
            listOf(
                "onResponseReceivedCommands",
                0,
                "appendContinuationItemsAction",
                "continuationItems",
                0,
                "itemSectionRenderer",
                "contents"
            ),
            JSONArray()
        ) as JSONArray
    }



    if (flags=="playlist"){
        return safeGet(
            resoponseContext,
            listOf(
                "contents",
                "twoColumnBrowseResultsRenderer",
                "tabs", 0,
                "tabRenderer",
                "content",
                "sectionListRenderer",
                "contents", 0,
                "itemSectionRenderer",
                "contents",
                0,
                "playlistVideoListRenderer",
                "contents"
            ),
            JSONArray()
        ) as JSONArray
    }

    if (flags=="playlist_continuation"){
        return safeGet(
            resoponseContext,
            listOf(
                "onResponseReceivedActions", 0,
                "appendContinuationItemsAction",
                "continuationItems"
            ),
            JSONArray()
        ) as JSONArray
    }


    if (flags=="watchInitial"){
        return safeGet(
            resoponseContext,
            listOf(
                "contents",
                "twoColumnWatchNextResults",
                "secondaryResults",
                "secondaryResults",
                "results"
            ),
            JSONArray()
        ) as JSONArray
    }

    if (flags=="watchContinuation"){
        return safeGet(
            resoponseContext,
            listOf(
                "onResponseReceivedEndpoints",
                0,
                "appendContinuationItemsAction",
                "continuationItems",
            ),
            JSONArray()
        ) as JSONArray
    }


    if (flags=="mwebsearch"){
        return safeGet(
            resoponseContext,
            listOf(
                "contents",
                "sectionListRenderer",
                "contents",
                0,
                "itemSectionRenderer",
                "contents"
            ),
            JSONArray()
        ) as JSONArray
    }
    if (flags=="mwebfeeds"){
        return safeGet(
            resoponseContext,
            listOf(
                "contents",
                "singleColumnBrowseResultsRenderer",
                "tabs",
                0,
                "tabRenderer",
                "content",
                "richGridRenderer",
                "contents"
            ),
            JSONArray()
        ) as JSONArray
    }
    if (flags=="mwebcontinuation"){
        return safeGet(
            resoponseContext,
            listOf(
                "onResponseReceivedActions",
                0,
                "appendContinuationItemsAction",
                "continuationItems"
            ),
            JSONArray()
        ) as JSONArray
    }
    return JSONArray()

}

fun getVideoFromContextRender(video: JSONObject): VideoItem{

    val title=safeGet(video,
        listOf(
            "headline", "runs", 0, "text"
        ),"Title Not found")



    val videoId =
        video.optString("videoId").takeIf { it.isNotEmpty() }
            ?: (safeGet(
                video,
                listOf("navigationEndpoint", "watchEndpoint", "videoId"),
                null
            ) as? String)
            ?: "videoIdnotfound"




    val chaanelName=safeGet(
        video,
        listOf(
            "shortBylineText", "runs", 0, "text"
        )
    )
    val chaanelPhoto=safeGet(
        video,
        listOf(
            "channelThumbnail",
            "channelThumbnailWithLinkRenderer",
            "thumbnail",
            "thumbnails",
            0,
            "url",
        )
    )

    val chaanelId=safeGet(
        video,
        listOf(
            "channelThumbnail",
            "channelThumbnailWithLinkRenderer", "navigationEndpoint",
            "browseEndpoint",
            "browseId"
        )
    )

    val views=safeGet(
        video,
        listOf(
            "shortViewCountText", "runs", 0, "text"
        )
    )

    val duration=safeGet(
        video,
        listOf(
            "lengthText", "runs", 0, "text"
        )
    )

    val publishedOn=safeGet(
        video,
        listOf(
            "publishedTimeText", "runs", 0, "text"
        )
    )
    return VideoItem(
        videoId,
        title as String,
        "",
        chaanelName as String?,
        channelAvtar = chaanelPhoto as String?,
        channelUrl = chaanelId as String?,
        views as String?,
        duration as String?,
        publishedOn = publishedOn as String?
    )
}

fun getShortsList(shortsArray: JSONArray): MutableList<VideoItem> {

    val shortsList = mutableListOf<VideoItem>()

    for (i in 0 until shortsArray.length()) {

        val item = shortsArray.getJSONObject(i).getJSONObject("shortsLockupViewModel")

        val videoId = safeGet(
            item,
            listOf("onTap", "innertubeCommand", "reelWatchEndpoint", "videoId"),
            ""
        ) as String

        val title = safeGet(
            item,
            listOf("overlayMetadata","primaryText","content"),
            "No title"
        ) as String

        val views =
            (safeGet(
                item,
                listOf("overlayMetadata", "secondaryText", "content"),
                null
            ) as? String)
                ?: (safeGet(
                    item,
                    listOf("shortViewCountText", "runs", 0, "text"),
                    null
                ) as? String)
                ?: "Views"


        shortsList.add(
            VideoItem(
                videoId,
                title,
                "",
                null,
                null,
                null,
                views,
                null,
                null
            )
        )
    }

    return shortsList
}

fun parseInitialData(responseContext: JSONObject, flags: String): Pair<MutableList<VideoItem>, String?> {

    val contents = getContentArray(responseContext, flags)


    val videosList = mutableListOf<VideoItem>()
    var continuationToken: String? = null

    for (i in 0 until contents.length()) {


        val item = contents.optJSONObject(i) ?: continue


        // ✅ 1. Direct video
        item.optJSONObject("videoWithContextRenderer")?.let {
            videosList.add(getVideoFromContextRender(it))
        }

        // ✅ 2. Rich item video
        (safeGet(
            item,
            listOf("richItemRenderer", "content", "videoWithContextRenderer"),
            null
        ) as? JSONObject)?.let {
            videosList.add(getVideoFromContextRender(it))
        }

        // ✅ 3. Shorts (UNIFIED)
        val shortsArray =
            (safeGet(item, listOf("gridShelfViewModel", "contents"), null) as? JSONArray)
                ?: (safeGet(
                    item,
                    listOf("richSectionRenderer", "content", "gridShelfViewModel", "contents"),
                    null
                ) as? JSONArray)

        shortsArray?.let {
            val shortsList = getShortsList(it)

            if (shortsList.isNotEmpty()) {
                videosList.add(
                    VideoItem(
                        shortsList[0].videoId,
                        title = "shorts",
                        shortsArray = shortsList
                    )
                )
            }
        }

        // ✅ 4. Continuation (BEST WAY)
        val token = safeGet(
            item,
            listOf(
                "continuationItemRenderer",
                "continuationEndpoint",
                "continuationCommand",
                "token"
            ),
            null
        ) as? String

        if (!token.isNullOrEmpty()) {
            continuationToken = token
        }
    }

    // ✅ 5. Fallback continuation (no hardcoded index)
    if (continuationToken.isNullOrEmpty()) {
        continuationToken = safeGet(
            responseContext,
            listOf(
                "contents",
                "sectionListRenderer",
                "contents"
            ),
            null
        )?.let { arr ->
            (arr as? JSONArray)?.let { array ->
                for (i in 0 until array.length()) {
                    val token = safeGet(
                        array.getJSONObject(i),
                        listOf(
                            "continuationItemRenderer",
                            "continuationEndpoint",
                            "continuationCommand",
                            "token"
                        ),
                        null
                    ) as? String

                    if (!token.isNullOrEmpty()) return@let token
                }
                null
            }
        }
    }


    return Pair(videosList,continuationToken)
}