package com.ranjan.expertclient.screens.playerscreen.utils

import com.ranjan.expertclient.models.PlaylistMetaData
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.browserscreen.parsers.getContentArray
import com.ranjan.expertclient.screens.browserscreen.safeGet
import org.json.JSONArray
import org.json.JSONObject

data class PlaylistInfo(
    val videos: MutableList<VideoItem>,
    val continuation: String?,
    val metaData: PlaylistMetaData?
)

fun prasePlaylist(root: JSONObject, flags: String): PlaylistInfo {

    val contentsArray = getContentArray(root, flags)
    val videosList = mutableListOf<VideoItem>()
    var token: String? = null
    var channelMetadata: PlaylistMetaData? = null

    for (i in 0 until contentsArray.length()) {
        val item = contentsArray.getJSONObject(i)

        if (item.has("playlistVideoRenderer")) {
            val videoId = safeGet(item, listOf("playlistVideoRenderer", "videoId")) as String
            val title = safeGet(
                item, listOf(
                    "playlistVideoRenderer", "title", "runs", 0, "text"
                )
            ) as String

            val duration = safeGet(
                item, listOf(
                    "playlistVideoRenderer", "lengthText", "simpleText"
                )
            ) as String

            val views = safeGet(
                item, listOf(
                    "playlistVideoRenderer", "videoInfo", "runs", 0, "text"
                )
            ) as String

            val publishedOn = safeGet(
                item, listOf(
                    "playlistVideoRenderer", "videoInfo", "runs", 2, "text"
                )
            ) as String

            val channelName = safeGet(
                item, listOf(
                    "playlistVideoRenderer", "shortBylineText", "runs", 0, "text"
                )
            ) as String

            videosList.add(
                VideoItem(
                    videoId,
                    title,
                    duration = duration,
                    thumbnail = null,
                    views = views,
                    publishedOn = publishedOn,
                    channelName = channelName,
                    channelUrl = "https://www.youtube.com",
                    playlistId = videoId
                )
            )
        }

        if (item.has("continuationItemRenderer")) {
            try {
                token = safeGet(
                    item, listOf(
                        "continuationItemRenderer",
                        "continuationEndpoint",
                        "continuationCommand",
                        "token"
                    )
                ) as String

            } catch (e: Exception) {
                token = safeGet(
                    item, listOf(
                        "continuationItemRenderer",
                        "continuationEndpoint",
                        "commandExecutorCommand",
                        "commands", 1,
                        "continuationCommand",
                        "token"
                    )
                ) as String
            }
        }
    }

    if (flags == "playlist") {
        val heroImages = safeGet(
            root, listOf(
                "header", "pageHeaderRenderer",
                "content", "pageHeaderViewModel",
                "heroImage",
                "contentPreviewImageViewModel",
                "image",
                "sources"
            ), JSONArray()
        ) as JSONArray

        val channelId = safeGet(
            root, listOf(
                "header", "pageHeaderRenderer",
                "content", "pageHeaderViewModel",
                "metadata", "contentMetadataViewModel",
                "metadataRows", 0,
                "metadataParts", 0,
                "avatarStack", "avatarStackViewModel", "rendererContext",
                "commandContext", "onTap",
                "innertubeCommand", "browseEndpoint", "browseId"
            ),"channelidnotfound"
        ) as String

        val playlistTitle = safeGet(
            root, listOf(
                "header", "pageHeaderRenderer", "pageTitle"
            )
        ) as String

        val channelAvatar = safeGet(
            root, listOf(
                "header", "pageHeaderRenderer",
                "content", "pageHeaderViewModel",
                "metadata", "contentMetadataViewModel",
                "metadataRows", 0,
                "metadataParts", 0,
                "avatarStack", "avatarStackViewModel",
                "avatars", 0,
                "avatarViewModel",
                "image",
                "sources", 0,
                "url"
            )
        ) as String

        val createdBy = safeGet(
            root, listOf(
                "header",
                "pageHeaderRenderer",
                "content",
                "pageHeaderViewModel",
                "metadata",
                "contentMetadataViewModel",
                "metadataRows",
                0,
                "metadataParts",
                0,
                "avatarStack",
                "avatarStackViewModel",
                "text",
                "content"
            )
        ) as String

        val views = safeGet(
            root, listOf(
                "header", "pageHeaderRenderer",
                "content", "pageHeaderViewModel",
                "metadata", "contentMetadataViewModel",
                "metadataRows", 1,
                "metadataParts", 2,
                "text", "content"
            )
        ) as String

        val videoCount = safeGet(
            root, listOf(
                "header", "pageHeaderRenderer",
                "content", "pageHeaderViewModel",
                "metadata", "contentMetadataViewModel",
                "metadataRows", 1,
                "metadataParts", 1,
                "text", "content"
            )
        ) as String



        if (heroImages.length() == 0) {
            channelMetadata = PlaylistMetaData(
                playlistTitle,
                channelId,
                channelAvtar = channelAvatar,
                heroImage = "",
                createdBy,
                videosCount = videoCount,
                views = views,


                )
        } else {
            val heroimage = heroImages.getJSONObject(heroImages.length() - 1).getString("url")
            channelMetadata = PlaylistMetaData(
                playlistTitle,
                channelId,
                channelAvtar = channelAvatar,
                heroImage = heroimage,
                createdBy,
                videosCount = videoCount,
                views = views,
            )
        }


    }
    return PlaylistInfo(
        videosList,
        token,
        metaData = channelMetadata
    )
}