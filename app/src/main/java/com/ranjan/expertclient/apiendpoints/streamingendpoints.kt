package com.ranjan.expertclient.apiendpoints

import com.ranjan.expertclient.apiendpoints.RandomStringGenerator.generateContentPlaybackNonce
import com.ranjan.expertclient.apiendpoints.RandomStringGenerator.generateTParameter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

fun androidPlayerResponse(
    cpn: String,
    visitorData: String,
    videoId: String,
    t: String
): Request {

    val url =
        "https://youtubei.googleapis.com/youtubei/v1/reel/reel_item_watch?prettyPrint=false&t=$t&id=$videoId&\$fields=playerResponse"

    val jsonBody = JSONObject().apply {

        put("context", JSONObject().apply {

            put("client", JSONObject().apply {
                put("clientName", "ANDROID")
                put("clientVersion", "21.03.36")
                put("clientScreen", "WATCH")

                put("platform", "MOBILE")
                put("osName", "Android")
                put("osVersion", "16")
                put("androidSdkVersion", 36)

                put("hl", "en-GB")
                put("gl", "GB")
                put("utcOffsetMinutes", 0)

                put("visitorData", visitorData)
            })

            put("request", JSONObject().apply {
                put("internalExperimentFlags", JSONArray())
                put("useSsl", true)
            })

            put("user", JSONObject().apply {
                put("lockedSafetyMode", false)
            })
        })

        put("playerRequest", JSONObject().apply {
            put("videoId", videoId)
            put("cpn", cpn)
            put("contentCheckOk", true)
            put("racyCheckOk", true)
        })

        put("disablePlayerResponse", false)
    }

    val headers = mapOf(
        "User-Agent" to "com.google.android.youtube/21.03.36 (Linux; U; Android 15; GB) gzip",
        "X-Goog-Api-Format-Version" to "2",
        "Content-Type" to "application/json",
        "Accept-Language" to "en-GB, en;q=0.9"
    )

    val body = jsonBody.toString()
        .toRequestBody("application/json".toMediaTypeOrNull())

    val builder = Request.Builder()
        .url(url)
        .post(body)

    headers.forEach { (k, v) -> builder.addHeader(k, v) }

    return builder.build()
}

fun getVisitorId(): String {

    val client = OkHttpClient()

    val url =
        "https://youtubei.googleapis.com/youtubei/v1/visitor_id?prettyPrint=false"

    val jsonBody = JSONObject().apply {

        put("context", JSONObject().apply {

            put("client", JSONObject().apply {

                put("clientName", "ANDROID")
                put("clientVersion", "21.03.36")

                put("clientScreen", "WATCH")
                put("platform", "MOBILE")

                put("osName", "Android")
                put("osVersion", "16")
                put("androidSdkVersion", 36)

                put("hl", "en-GB")
                put("gl", "GB")
                put("utcOffsetMinutes", 0)
            })

            put("request", JSONObject().apply {
                put("internalExperimentFlags", JSONArray())
                put("useSsl", true)
            })

            put("user", JSONObject().apply {
                put("lockedSafetyMode", false)
            })
        })
    }

    val headers = mapOf(
        "User-Agent" to "com.google.android.youtube/21.03.36 (Linux; U; Android 15; GB) gzip",
        "X-Goog-Api-Format-Version" to "2",
        "Content-Type" to "application/json",
        "Accept-Language" to "en-GB, en;q=0.9"
    )

    val body =
        jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

    val builder = Request.Builder()
        .url(url)
        .post(body)

    headers.forEach { (k, v) -> builder.addHeader(k, v) }

    val response = client.newCall(builder.build()).execute()

    val json = JSONObject(response.body!!.string())

    return json
        .getJSONObject("responseContext")
        .getString("visitorData")
}


fun getWebVisitorId(): String {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://www.youtube.com")
        .header(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/137.0.0.0 Safari/537.36"
        )
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw RuntimeException("HTTP ${response.code}")
        }

        val body = response.body?.string()
            ?: throw RuntimeException("Empty response")

        val regex = """"VISITOR_DATA":"([^"]+)"""".toRegex()
        val match = regex.find(body)
            ?: throw RuntimeException("VISITOR_DATA not found")

        return match.groupValues[1]
    }
}

fun vrPlayerResponse(videoId: String): JSONObject {
    val visitorData = getWebVisitorId()

    val client = OkHttpClient()

    val body = JSONObject().apply {
        put("context", JSONObject().apply {
            put("client", JSONObject().apply {
                put("clientName", "ANDROID_VR")
                put("clientVersion", "1.65.10")
                put("deviceMake", "Oculus")
                put("deviceModel", "Quest 3")
                put("androidSdkVersion", 32)
                put(
                    "userAgent",
                    "com.google.android.apps.youtube.vr.oculus/1.65.10 " +
                            "(Linux; U; Android 12L; " +
                            "eureka-user Build/SQ3A.220605.009.A1) gzip"
                )
                put("osName", "Android")
                put("osVersion", "12L")
                put("hl", "en")
                put("timeZone", "UTC")
                put("utcOffsetMinutes", 0)
                put("visitorData", visitorData)
            })
        })

        put("videoId", videoId)

        put("playbackContext", JSONObject().apply {
            put("contentPlaybackContext", JSONObject().apply {
                put("html5Preference", "HTML5_PREF_WANTS")
                put("signatureTimestamp", 20594) // update if needed
            })
        })

        put("contentCheckOk", true)
        put("racyCheckOk", true)
    }

    val request = Request.Builder()
        .url("https://www.youtube.com/youtubei/v1/player?prettyPrint=false")
        .header("Content-Type", "application/json")
        .header("X-YouTube-Client-Name", "28")
        .header("X-YouTube-Client-Version", "1.65.10")
        .header("Origin", "https://www.youtube.com")
        .header(
            "User-Agent",
            "com.google.android.apps.youtube.vr.oculus/1.65.10 " +
                    "(Linux; U; Android 12L; " +
                    "eureka-user Build/SQ3A.220605.009.A1) gzip"
        )
        .header("X-Goog-Visitor-Id", visitorData)
        .post(
            body.toString()
                .toRequestBody("application/json".toMediaType())
        )
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw RuntimeException("HTTP ${response.code}")
        }

        val responseBody = response.body?.string()
            ?: throw RuntimeException("Empty response")

        return JSONObject(responseBody)
    }
}

fun getStreamingData(videoId: String,visitorData: String): JSONObject {
    val requestResponse = JSONObject()
    val cpn = generateContentPlaybackNonce()
    val tp = generateTParameter()

    val request = androidPlayerResponse(cpn, visitorData, videoId, tp)
    val client = OkHttpClient()


    try {
        val response = client.newCall(request).execute()
        if (response.code == 200) {
            val responseString = response.body?.string()
            return JSONObject(responseString)
        } else {

            requestResponse.put("error", "Returning fail: HTTP ${response.code}")
        }
    } catch (e: Exception) {

        requestResponse.put("error", e.message ?: "Unknown error")
    }
    return JSONObject()
}

fun getUrlByItag(adaptiveFormats: JSONArray, itag: Int): String? {
    for (i in 0 until adaptiveFormats.length()) {
        val format = adaptiveFormats.getJSONObject(i)

        if (format.optInt("itag") == itag) {

            // direct url case
            val url = format.optString("url")
            if (url.isNotEmpty()) {
                return url
            }

            // cipher case (url inside signatureCipher)
            val cipher = format.optString("signatureCipher")
            if (cipher.isNotEmpty()) {
                val params = cipher.split("&")
                for (p in params) {
                    if (p.startsWith("url=")) {
                        return java.net.URLDecoder.decode(
                            p.removePrefix("url="),
                            "UTF-8"
                        )
                    }
                }
            }
        }
    }

    return null
}
