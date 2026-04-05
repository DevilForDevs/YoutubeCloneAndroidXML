package com.ranjan.expertclient.screens.playerscreen.utils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WatchNextBrowse {

    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/143.0.0.0"

    private val httpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    fun getSuggestions(
        videoId: String,
        continuation: String?,
        visitorData: String,
        clientVersion: String,
    ): JSONObject {
        val body =
            mutableMapOf(
                "context" to
                        mapOf(
                            "client" to
                                    mapOf(
                                        "hl" to "en",
                                        "gl" to "IN",
                                        "clientName" to "WEB",
                                        "clientVersion" to clientVersion,
                                        "platform" to "DESKTOP",
                                        "osName" to "Windows",
                                        "osVersion" to "10.0",
                                        "timeZone" to "Asia/Calcutta",
                                        "userAgent" to USER_AGENT,
                                        "visitorData" to visitorData,
                                    ),
                        ),
                "videoId" to videoId,
            )

        if (continuation != null) {
            body["continuation"] = continuation
        }

        val json = JSONObject(body).toString()

        val request =
            Request
                .Builder()
                .url("https://www.youtube.com/youtubei/v1/next?prettyPrint=false")
                .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .addHeader("content-type", "application/json")
                .addHeader("origin", "https://www.youtube.com")
                .addHeader("referer", "https://www.youtube.com/watch?v=$videoId")
                .addHeader("user-agent", USER_AGENT)
                .addHeader("x-youtube-client-name", "1")
                .addHeader("x-youtube-client-version", clientVersion)
                .addHeader("x-youtube-bootstrap-logged-in", "false")
                .addHeader("x-goog-visitor-id", visitorData)
                .build()

        httpClient.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string() ?: "{}"

            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}")
            }
            val jsonObj = JSONObject(bodyStr)
            // ⚠️ Detect silent failure (invalid client version)
            if (!jsonObj.has("contents") && continuation == null) {
                throw Exception("Invalid response (possible outdated client)")
            }

            return jsonObj
        }
    }


}
