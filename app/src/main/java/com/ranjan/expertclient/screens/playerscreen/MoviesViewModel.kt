package com.ranjan.expertclient.screens.playerscreen

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.moviesitesxtractors.SitesManager
import com.ranjan.expertclient.screens.playerscreen.models.DownloadItem
import com.ranjan.expertclient.utils.getOkHttpClient
import com.ranjan.expertclient.utils.txt2filename
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicLong

class MoviesViewModel : ViewModel() {
    private val siteManager = SitesManager()
    private val _downloads = MutableLiveData<List<DownloadItem>>()
    val downloads: LiveData<List<DownloadItem>> = _downloads

    private var loadJob: Job? = null
    private val latestRequestId = AtomicLong(0L)
    private val client = getOkHttpClient()

    // Track active jobs by URL
    private val activeJobs = mutableMapOf<String, Job>()
    
    // Persistent registry of download states
    private val downloadRegistry = mutableMapOf<String, DownloadItem>()
    
    private var currentVideoUrls = listOf<String>()

    fun loadVideo(item: VideoItem, context: Context, showDialog: () -> Unit) {
        val pageUrl = item.pageUrl ?: return
        loadJob?.cancel()
        
        currentVideoUrls = emptyList()
        syncDownloadsWithRegistry()

        val requestId = latestRequestId.incrementAndGet()
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            val urls = siteManager.getVideoUrls(pageUrl, context)
            if (requestId != latestRequestId.get()) return@launch
            
            val validUrls = mutableListOf<String>()
            urls.forEach { streamItem ->
                if (requestId != latestRequestId.get()) return@launch
                val isValid = streamItem.url.contains("jio=", ignoreCase = true)
                if (isValid) {
                    validUrls.add(streamItem.url)
                    
                    val sanitizedId = txt2filename(item.videoId)
                    val sanitizedTitle = txt2filename(item.title)
                    val fileName = "${sanitizedId}_${sanitizedTitle}_${streamItem.resolutionString ?: "auto"}.mp4"

                    val file = File(context.filesDir, fileName)
                    val currentLength = if (file.exists()) file.length() else 0L
                    
                    if (!downloadRegistry.containsKey(streamItem.url)) {
                        downloadRegistry[streamItem.url] = DownloadItem(
                            resolution = streamItem.resolutionString ?: "auto",
                            downloaded = currentLength,
                            total = 0L,
                            isPlaying = false,
                            isDownloading = activeJobs.containsKey(streamItem.url),
                            fileUrl = streamItem.url,
                            speed = "",
                            fileName = if(file.exists()) file.absolutePath else fileName,
                            isFinished =true,
                            status = when {
                                activeJobs.containsKey(streamItem.url) -> "Downloading..."
                                currentLength > 0 -> "Paused"
                                else -> "Queued ${streamItem.size ?: ""}"
                            }
                        )
                    } else {
                        val existing = downloadRegistry[streamItem.url]!!
                        downloadRegistry[streamItem.url] = existing.copy(
                            isDownloading = activeJobs.containsKey(streamItem.url),
                            downloaded = currentLength,
                            status = when {
                                activeJobs.containsKey(streamItem.url) -> "Downloading..."
                                existing.isFinished -> "Downloaded"
                                currentLength > 0 -> "Paused"
                                else -> existing.status
                            }
                        )
                    }
                }
            }
            
            if (requestId == latestRequestId.get()) {
                currentVideoUrls = validUrls
                syncDownloadsWithRegistry()
                withContext(Dispatchers.Main) {
                    showDialog()
                }
            }
        }
    }

    private fun syncDownloadsWithRegistry() {
        val listToShow = currentVideoUrls.mapNotNull { downloadRegistry[it] }
        _downloads.postValue(listToShow)
    }

    override fun onCleared() {
        loadJob?.cancel()
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
        super.onCleared()
    }

    fun action(item: DownloadItem, context: Context) {
        if (activeJobs.containsKey(item.fileUrl)) {
            activeJobs[item.fileUrl]?.cancel()
            activeJobs.remove(item.fileUrl)
            updateDownloadState(item.fileUrl) { it.copy(isDownloading = false, status = "Paused") }
            return
        }

        val job = viewModelScope.launch(Dispatchers.IO) {
            var success = false
            var retryCount = 0
            val maxRetries = 3
            
            while (retryCount < maxRetries && !success) {
                var randomAccessFile: RandomAccessFile? = null
                try {
                    val file = File(context.filesDir, item.fileName)
                    val existingBytes = if (file.exists()) file.length() else 0L

                    updateDownloadState(item.fileUrl) { 
                        it.copy(isDownloading = true, status = if (retryCount > 0) "Retrying ($retryCount)..." else "Connecting...") 
                    }

                    val requestBuilder = Request.Builder().url(item.fileUrl)
                    if (existingBytes > 0) {
                        requestBuilder.addHeader("Range", "bytes=$existingBytes-")
                    }
                    
                    val response = client.newCall(requestBuilder.build()).execute()
                    
                    if (!response.isSuccessful || response.body == null) {
                        updateDownloadState(item.fileUrl) { 
                            it.copy(isDownloading = false, status = "Failed: ${response.code}") 
                        }
                        return@launch
                    }

                    val body = response.body!!
                    val contentLength = body.contentLength()
                    val totalBytes = if (response.code == 206) contentLength + existingBytes else contentLength
                    
                    updateDownloadState(item.fileUrl) { 
                        it.copy(total = totalBytes, status = "Downloading") 
                    }

                    val inputStream = body.byteStream()
                    randomAccessFile = RandomAccessFile(file, "rw")
                    randomAccessFile.seek(existingBytes)
                    
                    val buffer = ByteArray(64 * 1024)
                    var bytesRead: Int
                    var downloadedSoFar = existingBytes
                    var lastUpdate = 0L

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        randomAccessFile.write(buffer, 0, bytesRead)
                        downloadedSoFar += bytesRead
                        
                        val now = System.currentTimeMillis()
                        if (now - lastUpdate > 500) {
                            updateDownloadState(item.fileUrl) { it.copy(downloaded = downloadedSoFar, total = totalBytes) }
                            lastUpdate = now
                        }
                    }

                    inputStream.close()

                    val isCompleted = downloadedSoFar >= totalBytes && totalBytes > 0
                    updateDownloadState(item.fileUrl) { 
                        it.copy(
                            isDownloading = false, 
                            isFinished = isCompleted, 
                            downloaded = downloadedSoFar, 
                            status = if (isCompleted) "Downloaded" else "Paused",
                            fileName = if (isCompleted) file.absolutePath else item.fileName
                        ) 
                    }
                    success = true

                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    
                    retryCount++
                    if (retryCount >= maxRetries) {
                        e.printStackTrace()
                        updateDownloadState(item.fileUrl) { 
                            it.copy(isDownloading = false, status = "Error: ${e.localizedMessage}") 
                        }
                    } else {
                        updateDownloadState(item.fileUrl) { it.copy(status = "Retrying...") }
                        delay(2000L * retryCount)
                    }
                } finally {
                    try { randomAccessFile?.close() } catch (ignored: Exception) {}
                }
            }
            activeJobs.remove(item.fileUrl)
        }
        activeJobs[item.fileUrl] = job
    }

    private fun updateDownloadState(url: String, transform: (DownloadItem) -> DownloadItem) {
        val existing = downloadRegistry[url] ?: return
        val updated = transform(existing)
        downloadRegistry[url] = updated
        
        if (currentVideoUrls.contains(url)) {
            syncDownloadsWithRegistry()
        }
    }
}
