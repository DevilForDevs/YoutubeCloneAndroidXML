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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MoviesScreenViewModel : ViewModel() {

    private val sitesManager = SitesManager()

    private val _state = MutableLiveData<VideoNavState>()
    val state: LiveData<VideoNavState> = _state
    var nextPageUrl: String?=null
    val error = MutableLiveData<String?>()
    private var stateVersion = 0



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

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val items = sitesManager.getFeeds(siteItem, context)
                nextPageUrl=items.nextPageUrl

                postState(
                    VideoNavState(
                        currentPage = VideoPage(
                            items = items.items,
                            title = "Home",
                            sourceId = siteItem.toString()
                        ),
                        history = emptyList(),
                        isLoading = false
                    )
                )
            }catch (e: Exception){
                error.postValue(e.message)
            }
        }
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
                val items = sitesManager.getPage(item.pageUrl?:"", context)
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
                sitesManager.getPage(url, context)
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