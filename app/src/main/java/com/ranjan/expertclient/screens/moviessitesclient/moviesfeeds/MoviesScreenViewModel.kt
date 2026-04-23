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

    // -------------------------
    // Load root (first screen)
    // -------------------------
    fun loadRoot(siteItem: SiteItem, context: Context) {
        _state.value = VideoNavState(
            currentPage = VideoPage(emptyList(), "Loading"),
            isLoading = true
        )

        viewModelScope.launch(Dispatchers.IO) {
            val items = sitesManager.getFeeds(siteItem, context)

            _state.postValue(
                VideoNavState(
                    currentPage = VideoPage(
                        items = items,
                        title = "Home",
                        sourceId = siteItem.toString()
                    ),
                    history = emptyList(),
                    isLoading = false
                )
            )
        }
    }

    // -------------------------
    // Handle item click
    // -------------------------
    fun onItemClick(item: VideoItem, context: Context) {

        // Only navigate if it's a category
        if (!item.category) return

        val currentState = _state.value ?: return

        // push current page into history
        val newHistory = currentState.history + currentState.currentPage

        _state.value = currentState.copy(isLoading = true)

        viewModelScope.launch(Dispatchers.IO) {


            val items = sitesManager.getPage(item.pageUrl?:"", context)
            val newPage = VideoPage(
                items = items,
                title = item.title,
                sourceId = item.playlistId ?: item.pageUrl
            )

            _state.postValue(
                VideoNavState(
                    currentPage = newPage,
                    history = newHistory,
                    isLoading = false
                )
            )
        }
    }

    // -------------------------
    // Back navigation
    // -------------------------
    fun goBack() : Boolean{
        val currentState = _state.value ?: return false
        if (currentState.history.isEmpty()) return false

        val previousPage = currentState.history.last()
        val newHistory = currentState.history.dropLast(1)

        _state.value = currentState.copy(
            currentPage = previousPage,
            history = newHistory
        )

        return true
    }

}