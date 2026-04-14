package com.ranjan.expertclient.screens.playerscreen.utils

import com.ranjan.expertclient.screens.playerscreen.models.StreamItem
import org.json.JSONObject

fun getFmtList(streamingData: JSONObject): List<StreamItem>{
    val array = streamingData
        .getJSONObject("playerResponse")
        .getJSONObject("streamingData")
        .getJSONArray("adaptiveFormats")
    return parseAdaptiveFormats(array).reversed()
}