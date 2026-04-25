package com.ranjan.expertclient.screens.playerscreen

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.moviesitesxtractors.SitesManager
import com.ranjan.expertclient.screens.playerscreen.models.DownloadItem
import com.ranjan.expertclient.utils.convertBytes
import com.ranjan.expertclient.utils.fetchUrlInfo
import com.ranjan.expertclient.utils.resumableInOneGoDownloader
import com.ranjan.expertclient.utils.txt2filename
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.cancellation.CancellationException

class MoviesViewModel : ViewModel() {

    private val siteManager = SitesManager()
    private val _downloads = MutableLiveData<List<DownloadItem>>()
    val downloads: LiveData<List<DownloadItem>> = _downloads

    private var loadJob: Job? = null
    private val latestRequestId = AtomicLong(0L)

    // Track active jobs by URL
    private val activeJobs = mutableMapOf<String, Job>()
    
    private var currentVideoUrls = listOf<String>()

    fun buildUserAgent(): String {
        val androidVersion = android.os.Build.VERSION.RELEASE
        val device = android.os.Build.MODEL
        val brand = android.os.Build.BRAND
        val buildId = android.os.Build.ID

        return "Mozilla/5.0 (Linux; Android $androidVersion; $device Build/$buildId; wv) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 " +
                "Chrome/120.0.0.0 Mobile Safari/537.36"
    }

    fun loadVideo(item: VideoItem, context: Context, showDialog: () -> Unit) {
        val pageUrl = item.pageUrl ?: return
        loadJob?.cancel()
        
        currentVideoUrls = emptyList()
        val requestId = latestRequestId.incrementAndGet()
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            val urls = siteManager.getVideoUrls(pageUrl, context)
            if (requestId != latestRequestId.get()) return@launch
            
            val di=mutableListOf<DownloadItem>()
            val sitesFolder=File(context.filesDir,item.siteName?:"default")
            if (!sitesFolder.exists()){
                sitesFolder.mkdir()
            }
            val thumbnailsFolder=File(sitesFolder,"thumbnails")
            val videosFolder=File(sitesFolder,"videos")
            if (!thumbnailsFolder.exists()){
                thumbnailsFolder.mkdir()
            }
            if (!videosFolder.exists()){
                videosFolder.mkdir()
            }
            val sanitizedTitle = txt2filename(item.title)
            val thumbnailFile = File(thumbnailsFolder, "${sanitizedTitle}.jpg")


            urls.forEach { streamItem ->
                if (requestId != latestRequestId.get()) return@launch
                val isValid = streamItem.url.contains("jio=", ignoreCase = true)
                if (isValid) {
                    val videoFile = File(videosFolder, "${sanitizedTitle}(${streamItem.resolutionString}).mp4")
                    if (videoFile.exists()){
                        println("Video already exists: ${videoFile.length()}")
                    }
                    val finalHeaders = streamItem.headers.toMutableMap().apply {
                        put("User-Agent", buildUserAgent())
                    }

                    di.add(DownloadItem(
                        resolution = streamItem.resolutionString?:"auto",
                        dbyBydt =if (thumbnailFile.exists()) convertBytes(videoFile.length()) else "${convertBytes(videoFile.length())}/${streamItem.size}",
                        speed = "512/kb",
                        isDownloading = false,
                        fileName = videoFile.absolutePath,
                        fileUrl = streamItem.url,
                        isPlaying = false,
                        isFinished = thumbnailFile.exists(),
                        status = "Queued ${streamItem.size}",
                        headers = finalHeaders
                    ))
                }
            }
            
            if (requestId == latestRequestId.get()) {
                _downloads.postValue(di)
                withContext(Dispatchers.Main) {
                    showDialog()
                }
            }
        }
    }
    fun action(item: DownloadItem) {

        // 🔁 Toggle cancel
        if (activeJobs.containsKey(item.fileUrl)) {
            activeJobs[item.fileUrl]?.cancel()
            activeJobs.remove(item.fileUrl)

            updateDownloadItem(item.fileUrl) {
                it.copy(
                    isDownloading = false,
                    status = "Cancelled"
                )
            }
            return
        }

        val job = viewModelScope.launch(Dispatchers.IO) {
            println("starting")

            updateDownloadItem(item.fileUrl) {
                it.copy(
                    isDownloading = true,
                    status = "Starting..."
                )
            }

            val urlInfo = fetchUrlInfo(item.fileUrl, emptyMap(), ::onError)

            if (urlInfo?.mimeType == null || !urlInfo.mimeType.startsWith("video/")) {
                updateDownloadItem(item.fileUrl) {
                    it.copy(status = "Not a video")
                }
                return@launch
            }

            val file = File(item.fileName)
            val exists = file.exists()
            val fos = FileOutputStream(file, exists)


            try {
                resumableInOneGoDownloader(
                    url = item.fileUrl,
                    fos = fos,
                    onDisk = if (exists) file.length() else 0,
                    totalBytes = urlInfo.contentLength,
                    headers = item.headers,

                    progress = { dbyt, percent, speed ->

                        updateDownloadItem(item.fileUrl) {
                            it.copy(
                                dbyBydt = dbyt,
                                speed = speed,
                                progressPercent = percent,
                                status = "Downloading..."
                            )
                        }
                    },

                    onError = { error ->

                        updateDownloadItem(item.fileUrl) {
                            it.copy(
                                isDownloading = false,
                                status = "Error: $error"
                            )
                        }
                    }
                )

            } catch (e: CancellationException) {

                updateDownloadItem(item.fileUrl) {
                    it.copy(
                        isDownloading = false,
                        status = "Cancelled"
                    )
                }

            } finally {
                activeJobs.remove(item.fileUrl)
            }
        }

        // ✅ IMPORTANT: store job
        activeJobs[item.fileUrl] = job
    }

    fun onError(message: String){
        println(message)
    }
    private fun updateDownloadItem(
        fileUrl: String,
        transform: (DownloadItem) -> DownloadItem
    ) {
        val currentList = _downloads.value?.toMutableList() ?: return

        val index = currentList.indexOfFirst { it.fileUrl == fileUrl }
        if (index == -1) return

        currentList[index] = transform(currentList[index])
        _downloads.postValue(currentList)
    }



}
