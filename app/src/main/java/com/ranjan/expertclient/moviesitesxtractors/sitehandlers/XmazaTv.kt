package com.ranjan.expertclient.moviesitesxtractors.sitehandlers

import com.ranjan.expertclient.apiendpoints.HtmlExtractor
import com.ranjan.expertclient.models.PraseResult
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.moviesitesxtractors.SiteParser
import com.ranjan.expertclient.screens.browserscreen.safeGet
import com.ranjan.expertclient.screens.playerscreen.models.StreamItem
import com.ranjan.expertclient.utils.getOkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class XmazaTv(
    override var localSchemaFolder: String?,
    override val siteTitle: String
) : SiteParser {

    val githubSchemasFolder = "https://raw.githubusercontent.com/DevilForDevs/YoutubeCloneAndroidXML/master/schemas/mp4moviez/"
    private val client = getOkHttpClient()



    override fun getFeeds(
        url: String,
        schemasFolder: String
    ): PraseResult {

        localSchemaFolder=schemasFolder
        val fileName="Feeds.json"
        val feedsSchemaUrl=githubSchemasFolder+fileName
        val cacheFile = File(schemasFolder, fileName)
        val jsonBody = if (cacheFile.exists()) {
            cacheFile.readText()
        } else {
            val fetchedJson = fetchSitesJson(feedsSchemaUrl)
            cacheFile.writeText(fetchedJson)
            fetchedJson
        }

        val input = JSONObject().apply {
            put("url", url)
            put("schema", JSONObject(jsonBody))
        }
        val jsonResult = HtmlExtractor.fetch(input)
        return parseResult(jsonResult)

    }

    override fun getPage(url: String): PraseResult {
        TODO("Not yet implemented")
    }

    override fun getVideoUrls(url: String): MutableList<StreamItem> {
        TODO("Not yet implemented")
    }
    private fun fetchSitesJson(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw okio.IOException("Network error") as Throwable
            return response.body?.string() ?: "{}"
        }
    }

    private fun parseResult(jsonObject: JSONObject): PraseResult {
        val items = mutableListOf<VideoItem>()
        val videos = safeGet(
            jsonObject,
            listOf("sections", "videos", "items"),
            JSONArray()
        ) as? JSONArray ?: JSONArray()




        return PraseResult(
            items = items,
            nextPageUrl = ""
        )
    }
}