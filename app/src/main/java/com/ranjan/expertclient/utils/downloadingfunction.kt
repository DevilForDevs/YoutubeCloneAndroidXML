package com.ranjan.expertclient.utils

import com.ranjan.expertclient.models.UrlInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.FileOutputStream
import kotlin.coroutines.cancellation.CancellationException

suspend fun resumableInOneGoDownloader(
    url: String,
    fos: FileOutputStream,
    onDisk: Long,
    totalBytes: Long,
    headers: Map<String, String> = emptyMap(),
    progress: (dbyt: String, percent: Int, speed: String) -> Unit,
    onError: (message: String) -> Unit
) {
    val client = getOkHttpClient()
    val context = currentCoroutineContext()

    val maxRetries = 3
    var attempt = 0
    var currentOnDisk = onDisk

    while (attempt < maxRetries) {
        try {
            val requestBuilder = Request.Builder().url(url)

            // ✅ Resume support (updated offset)
            if (currentOnDisk > 0) {
                requestBuilder.addHeader("Range", "bytes=$currentOnDisk-")
            }

            headers.forEach { (k, v) ->
                requestBuilder.addHeader(k, v)
            }

            val call = client.newCall(requestBuilder.build())

            context[kotlinx.coroutines.Job]?.invokeOnCompletion {
                call.cancel()
            }

            val response = call.execute()

            if (!response.isSuccessful) {
                if (response.code in 500..599) {
                    throw java.io.IOException("Server error ${response.code}")
                } else {
                    onError("HTTP error ${response.code}")
                    return
                }
            }

            if (currentOnDisk > 0 && response.code == 200) {
                onError("(Range ignored) Resume failed")
                return
            }

            response.body?.byteStream()?.use { inputStream ->
                val buffer = ByteArray(16 * 1024)
                var bytesRead: Int

                var downloaded = currentOnDisk
                var speedBytes = 0L
                var lastTime = System.currentTimeMillis()

                var lastDataTime = System.currentTimeMillis()
                val stallTimeout = 30_000L // 30 sec

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {

                    if (!context.isActive) throw CancellationException()

                    if (bytesRead > 0) {
                        lastDataTime = System.currentTimeMillis()
                    }

                    // ❗ detect stalled download
                    if (System.currentTimeMillis() - lastDataTime > stallTimeout) {
                        throw java.net.SocketTimeoutException("Download stalled")
                    }

                    fos.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                    speedBytes += bytesRead
                    currentOnDisk = downloaded // ✅ update for retry resume

                    val percent = if (totalBytes > 0)
                        ((downloaded * 100) / totalBytes).toInt()
                    else 0

                    val now = System.currentTimeMillis()

                    if (now - lastTime >= 1000) {
                        val speedText = convertSpeed(speedBytes)
                        val pg = "${convertBytes(downloaded)}/${convertBytes(totalBytes)}"
                        progress(pg, percent, speedText)

                        speedBytes = 0
                        lastTime = now
                    }
                }

                val percent = if (totalBytes > 0)
                    ((downloaded * 100) / totalBytes).toInt()
                else 100

                val pg = "${convertBytes(downloaded)}/${convertBytes(totalBytes)}"
                progress(pg, percent, convertSpeed(speedBytes))
            }

            withContext(Dispatchers.IO) {
                fos.flush()
                fos.close()
            }

            return // ✅ success → exit retry loop

        } catch (e: CancellationException) {
            try {
                withContext(Dispatchers.IO) {
                    fos.close()
                }
            } catch (_: Exception) {}
            onError("Download Canceled")
            throw e

        } catch (e: java.net.SocketTimeoutException) {
            attempt++

            if (attempt >= maxRetries) {
                try {
                    withContext(Dispatchers.IO) {
                        fos.close()
                    }
                } catch (_: Exception) {}
                onError("Timeout after $maxRetries attempts")
                return
            }

            delay(1000L * attempt) // ⏳ backoff

        } catch (e: java.io.IOException) {
            attempt++

            if (attempt >= maxRetries) {
                try {
                    withContext(Dispatchers.IO) {
                        fos.close()
                    }
                } catch (_: Exception) {}
                onError(e.message ?: "Network error after retries")
                return
            }

            delay(1000L * attempt)

        } catch (e: Exception) {
            try {
                withContext(Dispatchers.IO) {
                    fos.close()
                }
            } catch (_: Exception) {}
            onError(e.message ?: "Unknown error")
            return
        }
    }
}

fun fetchUrlInfo(
    url: String,
    headers: Map<String, String> = emptyMap(),
    onError: (String) -> Unit
): UrlInfo? {

    val client = getOkHttpClient()

    fun buildRequest(method: String): Request {
        val builder = Request.Builder().url(url)

        headers.forEach { (k, v) -> builder.addHeader(k, v) }

        return if (method == "HEAD") {
            builder.head().build()
        } else {
            builder.get().build()
        }
    }

    try {
        // ✅ Try HEAD first
        var response = client.newCall(buildRequest("HEAD")).execute()

        // ❌ Some servers don't support HEAD
        if (!response.isSuccessful || response.body == null) {
            response.close()
            response = client.newCall(buildRequest("GET")).execute()
        }

        if (!response.isSuccessful) {
            onError("HTTP error: ${response.code}")
            return null
        }

        val headersMap = response.headers

        val contentLength = headersMap["Content-Length"]?.toLongOrNull() ?: -1L
        val mimeType = headersMap["Content-Type"]

        val disposition = headersMap["Content-Disposition"]
        val fileName = disposition
            ?.substringAfter("filename=", "")
            ?.replace("\"", "")

        val acceptRanges = headersMap["Accept-Ranges"]?.contains("bytes", true) == true

        response.close()

        return UrlInfo(
            contentLength = contentLength,
            mimeType = mimeType,
            fileName = fileName,
            acceptRanges = acceptRanges
        )

    } catch (e: Exception) {
        onError(e.message ?: "Unknown error")
        return null
    }
}