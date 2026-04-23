package com.ranjan.expertclient.moviesitesxtractors
import android.content.Context
import com.ranjan.expertclient.apiendpoints.HtmlExtractor
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.browserscreen.safeGet
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

class Mp4moviez {
    val schemaFolder = "https://raw.githubusercontent.com/DevilForDevs/YoutubeCloneAndroidXML/master/schemas/mp4moviez/"
    private val client by lazy { OkHttpClient() }


    fun getFeeds(url: String,context: Context):MutableList<VideoItem>{
        val fileName="Feeds.json"
        val feedsSchema=schemaFolder+fileName

        val cacheFile = File(context.filesDir, "mp4moviez$fileName")
        val jsonBody = if (cacheFile.exists()) {
            cacheFile.readText()
        } else {
            val fetchedJson = fetchSitesJson(feedsSchema)
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

    private fun fetchSitesJson(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Network error")
            return response.body?.string() ?: "{}"
        }
    }
    fun parseResult(jsonObject: JSONObject): MutableList<VideoItem>{
        val items = mutableListOf<VideoItem>()
        val movies = safeGet(
            jsonObject,
            listOf("sections", "movie_list", "items"),
            JSONArray()
        ) as? JSONArray ?: JSONArray()

        val categories = safeGet(
            jsonObject,
            listOf("sections", "movie_categories", "items"),
            JSONArray()
        ) as? JSONArray ?: JSONArray()

        val pagination = safeGet(
            jsonObject,
            listOf("sections", "pagination", "items"),
            JSONArray()
        ) as? JSONArray ?: JSONArray()

        for (i in 0 until movies.length()) {
            val movie = movies.optJSONObject(i) ?: continue
            println(movie)
            val title = movie.optString("title")
            val detailUrl = movie.optString("detail_url")
            val format = movie.optString("format")
            val category = movie.optString("category")
            val poster = movie.optString("poster")
            if (title.isNotBlank() || detailUrl.isNotBlank()) {
                items.add(
                    VideoItem(
                        videoId = detailUrl,
                        title = title.ifBlank { detailUrl },
                        thumbnail = poster.ifBlank { null },
                        pageUrl = detailUrl,
                        views = format.ifBlank { null },
                        publishedOn = category.ifBlank { null },
                        yt = false,
                        category = false
                    )
                )
            }
        }

        for (i in 0 until categories.length()) {
            val categoryItem = categories.optJSONObject(i) ?: continue
            val name = categoryItem.optString("name")
            val url = categoryItem.optString("url")
            val icon = categoryItem.optString("icon")
            val iconAlt = categoryItem.optString("icon_alt")
            if (name.isNotBlank() || url.isNotBlank()) {
                items.add(
                    VideoItem(
                        videoId = url,
                        title = name.ifBlank { url },
                        thumbnail = icon.ifBlank { null },
                        pageUrl = url,
                        publishedOn = iconAlt.ifBlank { null },
                        yt = false,
                        category = true

                    )
                )
            }
        }

        var nextPageUrl: String? = null
        for (i in 0 until pagination.length()) {
            val page = pagination.optJSONObject(i) ?: continue
            val pageNumber = page.optString("page_number")
            val pageUrl = page.optString("page_url")
            val type = page.optString("type")
            if (pageNumber.lowercase().contains("next")) {
                nextPageUrl = pageUrl
                break
            }
        }
        return items



    }






}