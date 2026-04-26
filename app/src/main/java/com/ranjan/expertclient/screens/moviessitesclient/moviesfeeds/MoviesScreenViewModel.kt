package com.ranjan.expertclient.screens.moviessitesclient.moviesfeeds


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.models.VideoNavState
import com.ranjan.expertclient.models.VideoPage
import com.ranjan.expertclient.moviesitesxtractors.SitesManager
import com.ranjan.expertclient.screens.sitesscreen.SiteItem
import com.ranjan.expertclient.utils.txt2filename
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MoviesScreenViewModel : ViewModel() {

    private val sitesManager = SitesManager()

    private val _state = MutableLiveData<VideoNavState>()
    val state: LiveData<VideoNavState> = _state
    var nextPageUrl: String?=null
    val error = MutableLiveData<String?>()
    private var stateVersion = 0
    var siteTitle=""



    override fun onCleared() {
        super.onCleared()
    }

    private fun setState(value: VideoNavState) {
        stateVersion += 1
        _state.value = value
    }

    private fun postState(value: VideoNavState) {
        stateVersion += 1
        _state.postValue(value)
    }


    // -------------------------
    // Load root (first screen)
    // -------------------------
    fun loadRoot(siteItem: SiteItem, context: Context) {
        setState(VideoNavState(
            currentPage = VideoPage(emptyList(), "Loading"),
            isLoading = true
        ))
        siteTitle=siteItem.title

        viewModelScope.launch(Dispatchers.IO) {
            val resultItems = try {
                val res = sitesManager.getFeeds(siteItem, context)
                nextPageUrl = res.nextPageUrl
                res.items
            } catch (e: Exception) {
                emptyList()
            }

            if (resultItems.isNotEmpty()) {
                postState(
                    VideoNavState(
                        currentPage = VideoPage(
                            items = resultItems,
                            title = "Home",
                            sourceId = siteItem.toString()
                        ),
                        history = emptyList(),
                        isLoading = false
                    )
                )
            } else {
                val localItems = getLocalDownloadedItems(siteItem, context)
                if (localItems.isNotEmpty()) {
                    postState(
                        VideoNavState(
                            currentPage = VideoPage(
                                items = localItems,
                                title = "Offline - Downloads",
                                sourceId = siteItem.toString()
                            ),
                            history = emptyList(),
                            isLoading = false
                        )
                    )
                } else {
                    error.postValue("No content found.")
                    postState(VideoNavState(currentPage = VideoPage(emptyList(), "Offline"), isLoading = false))
                }
            }
        }
    }

    private fun getLocalDownloadedItems(siteItem: SiteItem, context: Context): List<VideoItem> {
        val items = mutableListOf<VideoItem>()
        // ✅ The folder name is used in MoviesViewModel.kt as item.siteName?:"default"
        val folderName = if (siteItem.url.contains(siteItem.title, ignoreCase = true)) "Mp4moviez" else siteItem.title

        val sitesFolder = File(context.filesDir, folderName)
        val videosFolder = File(sitesFolder, "videos")
        val thumbnailsFolder = File(sitesFolder, "thumbnails")

        println("Offline check: Checking folder ${sitesFolder.absolutePath}")

        if (videosFolder.exists() && videosFolder.isDirectory) {
            videosFolder.listFiles()?.forEach { file ->
                if (file.isFile && (file.extension == "mp4" || file.extension == "mkv")) {
                    println("Offline check: Found file ${file.name}")
                    // Extract title by removing resolution part (e.g. "Title(720p).mp4")
                    val fileName = file.nameWithoutExtension
                    val title = if (fileName.contains("(")) {
                        fileName.substringBeforeLast("(")
                    } else {
                        fileName
                    }

                    // Look for thumbnail
                    val thumbFile = File(thumbnailsFolder, "${txt2filename(title)}.jpg")
                    
                    if (thumbFile.exists()) {
                        items.add(
                            VideoItem(
                                videoId = file.absolutePath,
                                title = title,
                                thumbnail = thumbFile.absolutePath,
                                pageUrl = file.absolutePath, // Use path as URL for playback
                                category = false,
                                siteName = folderName,
                                yt = false
                            )
                        )
                    }
                }
            }
        } else {
            println("Offline check: Folder does not exist or is not a directory")
        }
        return items.filter { it.title.isNotEmpty() }.distinctBy { it.title }
    }

    fun loadRootIfNeeded(siteItem: SiteItem, context: Context) {
        val currentState = _state.value
        val sourceId = siteItem.toString()
        val sameSiteInCurrent = currentState?.currentPage?.sourceId == sourceId
        val sameSiteInHistory = currentState?.history?.any { it.sourceId == sourceId } == true
        val belongsToSelectedSite = sameSiteInCurrent || sameSiteInHistory
        val hasUsableState = currentState?.currentPage?.items?.isNotEmpty() == true ||
            currentState?.history?.isNotEmpty() == true ||
            currentState?.isLoading == true

        // selectedSite LiveData re-emits on view recreation; avoid reloading same site state.
        if (currentState != null && belongsToSelectedSite && hasUsableState) {
            return
        }

        loadRoot(siteItem, context)
    }

    // -------------------------
    // Handle item click
    // -------------------------
    fun onItemClick(item: VideoItem, context: Context) {

        // Only navigate if it's a category
        if (!item.category) {
            return
        }

        val currentState = _state.value ?: run {
            return
        }

        // push current page into history
        val newHistory = currentState.history + currentState.currentPage

        setState(currentState.copy(isLoading = true))

        viewModelScope.launch(Dispatchers.IO) {


            try {
                val items = sitesManager.getPage(item.pageUrl?:"", context,siteTitle)
                val newPage = VideoPage(
                    items = items.items,
                    title = item.title,
                    sourceId = item.playlistId ?: item.pageUrl
                )
                nextPageUrl=items.nextPageUrl

                postState(
                    VideoNavState(
                        currentPage = newPage,
                        history = newHistory,
                        isLoading = false
                    )
                )
            }catch (e: Exception){
                error.postValue(e.message)
            }
        }
    }

    // -------------------------
    // Back navigation
    // -------------------------
    fun goBack() : Boolean{
        val currentState = _state.value ?: run {
            return false
        }
        if (currentState.history.isEmpty()) {
            return false
        }

        val previousPage = currentState.history.last()
        val newHistory = currentState.history.dropLast(1)

        setState(currentState.copy(
            currentPage = previousPage,
            history = newHistory
        ))

        return true
    }
    fun loadMore(context: Context) {
        val currentState = _state.value ?: run {
            return
        }
        if (currentState.isLoading) {
            return
        }

        val url = nextPageUrl
        if (url.isNullOrBlank()) {
            return
        }

        setState(currentState.copy(isLoading = true))

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                sitesManager.getPage(url, context,siteTitle)
            }.onSuccess { items ->
                nextPageUrl = items.nextPageUrl

                val updatedState = _state.value ?: currentState
                val mergedPage = updatedState.currentPage.copy(
                    items = updatedState.currentPage.items + items.items
                )

                postState(
                    updatedState.copy(
                        currentPage = mergedPage,
                        isLoading = false
                    )
                )
            }.onFailure {
                postState((_state.value ?: currentState).copy(isLoading = false))
                error.postValue(it.message)

            }
        }

    }

}