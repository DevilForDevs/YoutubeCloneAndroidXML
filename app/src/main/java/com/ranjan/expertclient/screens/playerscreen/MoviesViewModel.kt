package com.ranjan.expertclient.screens.playerscreen

import android.content.Context
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.moviesitesxtractors.SitesManager
import com.ranjan.expertclient.screens.playerscreen.models.DownloadItem
import com.ranjan.expertclient.screens.playerscreen.models.VideoDetails
import com.ranjan.expertclient.utils.convertBytes
import com.ranjan.expertclient.utils.downloadThumbnail
import com.ranjan.expertclient.utils.fetchUrlInfo
import com.ranjan.expertclient.utils.getOkHttpClient
import com.ranjan.expertclient.utils.resumableInOneGoDownloader
import com.ranjan.expertclient.utils.txt2filename
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
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
        val androidVersion = Build.VERSION.RELEASE
        val device = Build.MODEL
        val brand = Build.BRAND
        val buildId = Build.ID

        return "Mozilla/5.0 (Linux; Android $androidVersion; $device Build/$buildId; wv) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 " +
                "Chrome/120.0.0.0 Mobile Safari/537.36"
    }

    fun setupVideoInfo(psv: PlayerScreenViewModel, item: VideoItem) {
       psv.videoDetails.postValue(VideoDetails(
           title = item.title,
           likes = "Likes",
           dislikes = "",
           channelBigThumb = "",
           commentsCount = "",
           localLizedViewsandUploadedAgo = "",
           subscriberCount = "",
           firstHasTag ="",
           hashTags = "Playing Offline",
           channelName =item.siteName,
           channelUrl = ""
       ))



    }

    fun loadVideo(item: VideoItem, context: Context, showDialog: () -> Unit,psv: PlayerScreenViewModel) {
        val pageUrl = item.pageUrl ?: return
        setupVideoInfo(psv, item)

        // ✅ Check if it's a local file (Offline mode)
        val isLocalFile = File(pageUrl).exists() && (pageUrl.endsWith(".mp4") || pageUrl.endsWith(".mkv"))
        if (isLocalFile) {
            val file = File(pageUrl)
            
            // Access last 10 characters for resolution as requested
            val fileName = file.nameWithoutExtension
            val extractedResolution = if (fileName.length >= 10) fileName.takeLast(10) else fileName

            val di = listOf(
                DownloadItem(
                    resolution = extractedResolution,
                    dbyBydt = convertBytes(file.length()),
                    speed = "0 KB/s",
                    isDownloading = false,
                    fileName = pageUrl,
                    fileUrl = pageUrl,
                    isPlaying = false,
                    isFinished = true,
                    status = "Finished",
                    headers = emptyMap(),
                    thumbnailFile = item.thumbnail ?: "",
                    thumbnailUrl = null
                )
            )
            _downloads.postValue(di)
            viewModelScope.launch(Dispatchers.Main) {
                showDialog()
            }
            return
        }

        loadJob?.cancel()
        
        currentVideoUrls = emptyList()
        val requestId = latestRequestId.incrementAndGet()
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            val urls = siteManager.getVideoUrls(pageUrl, context,item.siteName?:"")
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
                        headers = finalHeaders,
                        thumbnailFile = thumbnailFile.absolutePath,
                        thumbnailUrl = item.thumbnail
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


            var success = true
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
                        success = false
                        updateDownloadItem(item.fileUrl) {
                            it.copy(
                                isDownloading = false,
                                status = "Error: $error"
                            )
                        }
                    }
                )

                if (success) {
                    // ✅ Download thumbnail after video download finishes
                    if (!item.thumbnailUrl.isNullOrEmpty()) {
                        val thumbFile = File(item.thumbnailFile)
                        if (!thumbFile.exists()) {
                            updateDownloadItem(item.fileUrl) {
                                it.copy(status = "Downloading thumbnail...")
                            }
                            downloadThumbnail(item.thumbnailUrl, thumbFile)
                        }
                    }

                    updateDownloadItem(item.fileUrl) {
                        it.copy(
                            isDownloading = false,
                            isFinished = true,
                            status = "Finished"
                        )
                    }
                }

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
