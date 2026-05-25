package com.ranjan.expertclient.screens.playerscreen

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.playerscreen.models.DownloadItem
import com.ranjan.expertclient.screens.playerscreen.models.VideoDetails
import com.ranjan.expertclient.utils.convertBytes
import com.ranjan.expertclient.utils.downloadThumbnail
import com.ranjan.expertclient.utils.fetchUrlInfo
import com.ranjan.expertclient.utils.resumableInOneGoDownloader
import com.ranjan.expertclient.utils.txt2filename
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.cancellation.CancellationException

class MoviesViewModel : ViewModel() {

    private val _downloads = MutableLiveData<List<DownloadItem>>()
    val downloads: LiveData<List<DownloadItem>> = _downloads

    // Track active jobs by URL
    private val activeJobs = mutableMapOf<String, Job>()

    fun buildUserAgent(): String {
        val androidVersion = Build.VERSION.RELEASE
        val device = Build.MODEL
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
           channelName = item.siteManager?.currentSite?.title,
           channelUrl = ""
       ))



    }

    fun loadVideo(item: VideoItem,showDialog: () -> Unit,psv: PlayerScreenViewModel) {
        val pageUrl = item.pageUrl ?: return
        setupVideoInfo(psv, item)
        if (item.siteManager==null) return

        val di=mutableListOf<DownloadItem>()
        val sitesFolder=item.siteManager.siteFolder

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
        if (thumbnailFile.exists()){
            val existingVideos = videosFolder.listFiles { file ->
                file.isFile && file.name.startsWith("${sanitizedTitle}(") && file.extension.equals("mp4", ignoreCase = true)
            }.orEmpty()

            existingVideos.forEach { videoFile ->
                val fileName = videoFile.nameWithoutExtension
                val extractedResolution = if (fileName.length >= 10) fileName.takeLast(10) else fileName

                di.add(
                    DownloadItem(
                        resolution = extractedResolution,
                        dbyBydt = convertBytes(videoFile.length()),
                        speed = "0 KB/s",
                        isDownloading = false,
                        fileName = videoFile.absolutePath,
                        fileUrl = pageUrl,
                        isPlaying = false,
                        isFinished = true,
                        status = "Offline Available",
                        headers = emptyMap(),
                        thumbnailFile = thumbnailFile.absolutePath,
                        thumbnailUrl = item.thumbnail
                    )
                )
            }
        }


        viewModelScope.launch(Dispatchers.IO) {
            val urls = item.siteManager.getVideoUrls(pageUrl)

            urls.forEach { streamItem ->
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
            _downloads.postValue(di)
            withContext(Dispatchers.Main) {
                showDialog()
            }

        }


    }
    fun action(item: DownloadItem) {
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
        if (item.status=="Offline Available"){
            return
        }

        val job = viewModelScope.launch(Dispatchers.IO) {

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

            } catch (_: CancellationException) {

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
