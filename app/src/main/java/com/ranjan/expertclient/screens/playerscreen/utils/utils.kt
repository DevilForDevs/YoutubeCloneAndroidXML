package com.ranjan.expertclient.screens.playerscreen.utils

import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.browserscreen.parsers.getContentArray
import com.ranjan.expertclient.screens.browserscreen.safeGet
import com.ranjan.expertclient.screens.playerscreen.models.StreamItem
import com.ranjan.expertclient.screens.playerscreen.widgets.models.InitialDataModel
import com.ranjan.expertclient.screens.playerscreen.widgets.models.VideoDetails
import org.json.JSONArray
import org.json.JSONObject

fun parseAdaptiveFormats(array: JSONArray): List<StreamItem> {
    val list = mutableListOf<StreamItem>()

    for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)

        val itag = obj.optInt("itag")
        val url = obj.optString("url")
        val mimeType = obj.optString("mimeType")
        val height = if (obj.has("height")) obj.optInt("height") else null
        val bitrate = obj.optInt("bitrate")

        if (url.isNullOrEmpty()) continue

        val isVideoAvc = mimeType.contains("video") && mimeType.contains("avc1")
        val isAudio = mimeType.contains("audio")

        if (!(isVideoAvc || isAudio)) continue

        list.add(
            StreamItem(
                itag = itag,
                url = url,
                mimeType = mimeType,
                height = height,
                bitrate = bitrate
            )
        )
    }

    return list
}


fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val seconds = (totalSeconds % 60).toInt()
    val minutes = ((totalSeconds / 60) % 60).toInt()
    val hours = (totalSeconds / 3600).toInt()

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

fun extractComments(root: JSONObject): String {
    val panels = safeGet(root, listOf("engagementPanels")) as? JSONArray ?: return ""

    for (i in 0 until panels.length()) {
        val contextualInfo = safeGet(
            panels.get(i),
            listOf(
                "engagementPanelSectionListRenderer",
                "header",
                "engagementPanelTitleHeaderRenderer",
                "contextualInfo"
            )
        )

        // 🔹 Normalize to runs array
        val runs: JSONArray? = when (contextualInfo) {
            is JSONArray -> contextualInfo
            is JSONObject -> contextualInfo.optJSONArray("runs")
            else -> null
        }

        if (runs != null && runs.length() > 0) {
            val first = runs.optJSONObject(0)
            val text = first?.optString("text")
            if (!text.isNullOrEmpty()) return text
        }
    }

    return ""
}

fun extractVideoDetails(root: JSONObject): VideoDetails? {
      try {
          val contents=safeGet(
              root,
              listOf(
                  "contents",
                  "twoColumnWatchNextResults",
                  "results",
                  "results",
                  "contents",
              )
          )as JSONArray





          val primaryInfo=safeGet(
              contents,
              listOf(
                  0,
                  "videoPrimaryInfoRenderer",
              )
          )



          val secondryInfo=safeGet(
              contents,
              listOf(
                  1,
                  "videoSecondaryInfoRenderer",
              )
          )


          val owner=safeGet(
              secondryInfo,
              listOf(
                  "owner",
                  "videoOwnerRenderer",
              )
          )

          val channelphoto=safeGet(
              secondryInfo,
              listOf(
                  "owner",
                  "videoOwnerRenderer",
                  "thumbnail",
                  "thumbnails", 1, "url"
              )
          )

          println(channelphoto)

          val subscriberCount=safeGet(
              owner,
              listOf(
                  "subscriberCountText",
                  "simpleText"
              )
          )

          val titleRun=safeGet(
              owner,
              listOf(
                  "title",
                  "runs",
                  0,
              )
          )
          val channelName=safeGet(
              titleRun,
              listOf(
                  "title",
                  "runs",
                  0,
              )
          )

          val channelUrl=safeGet(
              titleRun,
              listOf(
                  "navigationEndpoint",
                  "browseEndpoint",
                  "canonicalBaseUrl",
              )
          )
          val topBar=safeGet(
              primaryInfo,
              listOf(
                  "videoActions",
                  "menuRenderer",
                  "topLevelButtons",
              )
          )

          val likes=safeGet(
              topBar,
              listOf(
                  0,
                  "segmentedLikeDislikeButtonViewModel",
                  "likeButtonViewModel",
                  "likeButtonViewModel",
                  "toggleButtonViewModel",
                  "toggleButtonViewModel",
                  "defaultButtonViewModel",
                  "buttonViewModel",
                  "title",
              )
          )
          return VideoDetails(
              title = "title",
              likes as String,
              dislikes = "Dislikes",
              channelBigThumb = channelphoto as String,
              commentsCount = extractComments(root),
              localLizedViewsandUploadedAgo = "",
              subscriberCount = subscriberCount as String,
              firstHasTag = "",
              hashTags = "",
              channelName = channelName as String?,
              channelUrl = channelUrl as String?
          )

      }catch (e: Exception){
          return  null
      }
}

fun parseWatchItems(items: JSONArray): Pair<MutableList<VideoItem>, String?>{
    var continuationToken: String? = null
    val videosList=mutableListOf<VideoItem>()
    for (i in 0 until items.length()) {

        val item=items.getJSONObject(i)

        if (item.has("lockupViewModel")){
            val title= safeGet(item,listOf(
                "lockupViewModel",
                "metadata",
                "lockupMetadataViewModel",
                "title",
                "content",
            ),"No Title")

            val views=safeGet(item,listOf(
                "lockupViewModel",
                "metadata",
                "lockupMetadataViewModel",
                "metadata",
                "contentMetadataViewModel",
                "metadataRows",
                1,
                "metadataParts",
                0,
                "text",
                "content",
            ),"No views")

            val uploadedAgao=safeGet(item,listOf(
                "lockupViewModel",
                "metadata",
                "lockupMetadataViewModel",
                "metadata",
                "contentMetadataViewModel",
                "metadataRows",
                1,
                "metadataParts",
                1,
                "text",
                "content",
            ),"No uploadedAgo")

            val channelPhoto=safeGet(item,listOf(
                "lockupViewModel",
                "metadata",
                "lockupMetadataViewModel",
                "image",
                "decoratedAvatarViewModel",
                "avatar",
                "avatarViewModel",
                "image",
                "sources",
                0,
                "url",
            ),"No Title")

            val duration=safeGet(item,listOf(
                "lockupViewModel",
                "contentImage",
                "thumbnailViewModel",
                "overlays", 0,
                "thumbnailOverlayBadgeViewModel",
                "thumbnailBadges", 0,
                "thumbnailBadgeViewModel",
                "text"
            ),"No Title")

            val channelName = safeGet(item, listOf(
                "lockupViewModel",
                "metadata",
                "lockupMetadataViewModel",
                "metadata",
                "contentMetadataViewModel",
                "metadataRows",
                0,
                "metadataParts",
                0,
                "text",
                "content"
            ), "")

            val channelUrl = safeGet(item, listOf(
                "lockupViewModel",
                "metadata",
                "lockupMetadataViewModel",
                "image",
                "decoratedAvatarViewModel",
                "rendererContext",
                "commandContext",
                "onTap",
                "innertubeCommand",
                "browseEndpoint",
                "browseId"
            ), "")
            val contentType=safeGet(item,listOf(
               "lockupViewModel",
                "contentType"
            ),"")

            if (contentType=="LOCKUP_CONTENT_TYPE_VIDEO"){
                val videoId=safeGet(item,listOf(
                    "lockupViewModel",
                    "contentId"
                ))
                videosList.add(
                    VideoItem(
                        videoId as String,
                        title as String,
                        "",
                        channelName as String?,
                        channelAvtar = channelPhoto as String?,
                        channelUrl = channelUrl as String?,
                        views = views as String?,
                        duration = duration as String?,
                        playlistId = null,
                        shortsArray =null,
                        publishedOn = uploadedAgao as String?
                    )
                )
            }
            if (contentType=="LOCKUP_CONTENT_TYPE_PLAYLIST"){

                val videoId=safeGet(item,listOf(
                    "lockupViewModel",
                    "contentId"
                ))
                try {
                    val playlistId=safeGet(
                        item,listOf(
                            "lockupViewModel",
                            "rendererContext",
                            "commandContext",
                            "onTap",
                            "innertubeCommand",
                            "watchEndpoint",
                            "videoId"
                        )
                    )
                    videosList.add(
                        VideoItem(
                            videoId as String,
                            title as String,
                            "",
                            channelName as String?,
                            channelAvtar = channelPhoto as String?,
                            channelUrl = channelUrl as String?,
                            views = views as String?,
                            duration = duration as String?,
                            playlistId = playlistId as String?,
                            shortsArray =null,
                            publishedOn = uploadedAgao as String?
                        )
                    )

                }catch (e: Exception){
                    println(e.stackTrace)
                }
            }


        }
        if (item.has("reelShelfRenderer")) {

            val shortsList = mutableListOf<VideoItem>()

            val shortsItems = safeGet(
                item,
                listOf("reelShelfRenderer", "items"),
                JSONArray()
            ) as JSONArray

            for (i in 0 until shortsItems.length()) {

                val s = shortsItems.optJSONObject(i) ?: continue

                val rawId = safeGet(
                    s,
                    listOf("shortsLockupViewModel", "entityId"),
                    ""
                ) as String

                val videoId = rawId.removePrefix("shorts-shelf-item-")

                val title = safeGet(
                    s,
                    listOf(
                        "shortsLockupViewModel",
                        "overlayMetadata",
                        "primaryText",
                        "content"
                    ),
                    ""
                )

                val views = safeGet(
                    s,
                    listOf(
                        "shortsLockupViewModel",
                        "overlayMetadata",
                        "secondaryText",
                        "content"
                    ),
                    ""
                )

                shortsList.add(
                    VideoItem(
                        videoId,
                        title as String,
                        "",
                        null,
                        null,
                        null,
                        views as String?,
                        null,
                        null
                    )
                )

            }

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

        if (item.has("continuationItemRenderer")) {

            continuationToken = safeGet(
                item,
                listOf(
                    "continuationItemRenderer",
                    "continuationEndpoint",
                    "continuationCommand",
                    "token"
                ),
                ""
            ) as String?

        }
    }
    return Pair(videosList,continuationToken)
}

fun parseWatchHtml(response: JSONObject,flags: String): InitialDataModel{
    val contentsArray= getContentArray(response,flags)
    val pi=parseWatchItems(contentsArray)
    val chaanelDetails=extractVideoDetails(response)
    return InitialDataModel(
        videoDetails = chaanelDetails,
        videos = pi.first,
        continuation = pi.second
    )
}