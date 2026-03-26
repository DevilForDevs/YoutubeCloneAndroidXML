package com.ranjan.expertclient.screens.browserscreen.endpoints

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class HomePageBrowse(
    private val clientVersion: String,
    private val userAgent: String

){

    private val httpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()


    fun baseContext(
        visitorData: String,
        originalUrl: String,
    ): MutableMap<String, Any> =
        mutableMapOf(
            "context" to
                mapOf(
                    "client" to
                        mapOf(
                            "hl" to "en-GB",
                            "gl" to "NL",
                            "clientName" to "MWEB",
                            "clientVersion" to clientVersion,
                            "platform" to "MOBILE",
                            "osName" to "Android",
                            "osVersion" to "15",
                            "browserName" to "Chrome Mobile Webview",
                            "browserVersion" to "143.0.7499.34",
                            "visitorData" to visitorData,
                            "userAgent" to userAgent,
                            "clientFormFactor" to "SMALL_FORM_FACTOR",
                            "timeZone" to "Asia/Calcutta",
                            "originalUrl" to originalUrl,
                        ),
                ),
        )

    fun continueBrowse(
        continuation: String,
        visitorData: String,
    ): JSONObject {

        val body =
            baseContext(
                visitorData = visitorData,
                originalUrl = "https://m.youtube.com/",
            )

        body["continuation"] = continuation

        val json = JSONObject(body).toString()
        val requestBody =
            json.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request =
            Request
                .Builder()
                .url("https://m.youtube.com/youtubei/v1/browse?prettyPrint=false")
                .post(requestBody)
                .addHeader("content-type", "application/json")
                .addHeader("origin", "https://m.youtube.com")
                .addHeader("referer", "https://m.youtube.com/")
                .addHeader("user-agent", userAgent)
                .addHeader("x-youtube-client-name", "2")
                .addHeader("x-youtube-client-version", clientVersion)
                .addHeader("x-youtube-bootstrap-logged-in", "false")
                .addHeader("x-goog-visitor-id", visitorData)
                .build()

        httpClient.newCall(request).execute().use {
            return JSONObject(it.body?.string() ?: "{}")
        }
    }

}
