package com.ranjan.expertclient.moviesitesxtractors
import android.content.Context
import com.ranjan.expertclient.apiendpoints.HtmlExtractor
import com.ranjan.expertclient.models.PraseResult
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.browserscreen.safeGet
import com.ranjan.expertclient.screens.playerscreen.models.StreamItem
import com.ranjan.expertclient.utils.getOkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URI

class Mp4moviez {
    val schemaFolder = "https://raw.githubusercontent.com/DevilForDevs/YoutubeCloneAndroidXML/master/schemas/mp4moviez/"
    private val client = getOkHttpClient()

    fun getPage(url: String,context: Context): PraseResult{
        val fileName="CategoryPage.json"
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


    fun getFeeds(url: String,context: Context): PraseResult{
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
    fun parseResult(jsonObject: JSONObject): PraseResult {
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
                        category = false,
                        siteName = "Mp4moviez"
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
                        category = true,
                        siteName = "Mp4moviez"

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
       return PraseResult(
            items = items,
            nextPageUrl = nextPageUrl
       )




    }

    fun buildHdMovieUrl(inputUrl: String): String? {
        return try {
            // Extract origin manually
            val originMatch = Regex("^https?://[^/]+").find(inputUrl) ?: return null
            val origin = originMatch.value
            val path = inputUrl.removePrefix(origin)

            // match /c3160/
            val match = Regex("/c(\\d+)/").find(path) ?: return null
            val id = match.groupValues[1]

            // remove /c3160/
            var newPath = path.replace(Regex("/c\\d+/"), "/")

            // insert -hd-id before .html
            newPath = newPath.replace(Regex("\\.html$"), "-hd-$id.html")

            // encode safely (encodeURI equivalent — encode all except reserved/unreserved chars)
            val safePath = URI(null, null, newPath, null).toASCIIString()

            origin + safePath
        } catch (e: Exception) {
            null
        }
    }

    fun getBaseOrigin(url: String): String {
        val uri = java.net.URI(url)
        return "${uri.scheme}://${uri.host}"
    }

    fun getVideoUrls(url: String, context: Context): MutableList<StreamItem> {
        val murl = buildHdMovieUrl(url)
        if (murl.isNullOrEmpty()) return mutableListOf()

        val fileName = "Details.json"
        val feedsSchema = schemaFolder + fileName

        val cacheFile = File(context.filesDir, "mp4moviez$fileName")
        val jsonBody = if (cacheFile.exists()) {
            cacheFile.readText()
        } else {
            val fetchedJson = fetchSitesJson(feedsSchema)
            cacheFile.writeText(fetchedJson)
            fetchedJson
        }

        val input = JSONObject().apply {
            put("url", murl)
            put("schema", JSONObject(jsonBody))
        }

        val jsonResult = HtmlExtractor.fetch(input)

        val downloadLinks = safeGet(
            jsonResult,
            listOf("sections", "download_links", "items"),
            JSONArray()
        ) as JSONArray

        val variants = mutableListOf<StreamItem>()

        for (i in 0 until downloadLinks.length()) {
            val item = downloadLinks.getJSONObject(i)
            val videoUrl = item.getString("url")

            val origin = getBaseOrigin(videoUrl)

            variants.add(
                StreamItem(
                    itag = 0,
                    mimeType = "Mp4",
                    height = 0,
                    url = videoUrl,
                    bitrate = 80,
                    resolutionString = extractResolution(videoUrl),
                    size = extractBracketContent(item.getString("size")),

                    // ✅ Dynamic headers
                    headers = mapOf(
                        "Accept" to "*/*",
                        "Accept-Encoding" to "identity",
                        "Connection" to "keep-alive",
                        "Referer" to "$origin/",   // important
                        "Origin" to origin           // sometimes required
                    )
                )
            )
        }

        return variants
    }

    fun extractResolution(url: String): String? {
        val match = Regex("[?&]q=(\\d+)").find(url)
        return match?.groupValues?.get(1)
    }

    fun extractBracketContent(str: String): String? {
        val regex = Regex("\\[([^\\[\\]]+)]")
        val match = regex.find(str)
        return match?.groupValues?.get(1)?.trim()
    }


}