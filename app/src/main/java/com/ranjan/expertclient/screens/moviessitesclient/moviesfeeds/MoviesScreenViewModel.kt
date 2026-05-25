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
import java.io.File

class MoviesScreenViewModel : ViewModel() {

    private val sitesManager = SitesManager()

    private val _state = MutableLiveData<VideoNavState>()
    val state: LiveData<VideoNavState> = _state
    var nextPageUrl: String?=null
    val error = MutableLiveData<String?>()
    val isLoading = MutableLiveData(false)

    var currentSite: SiteItem?=null
    fun loadFeeds(siteItem: SiteItem,context: Context){
        if (currentSite!=null){
            return
        }
        isLoading.value=true
        currentSite=siteItem
        viewModelScope.launch(Dispatchers.IO){
            val sf= File(context.filesDir,siteItem.title )
            if (!sf.exists()){
                sf.mkdir()
            }
            val siteFolder=sf.absolutePath

            if (siteFolder==null){
                error.postValue("Failed to create schemas folder")
                isLoading.postValue(false)
                return@launch
            }else{
                val result=sitesManager.getFeeds(siteItem, siteFolder)
                if (result.items.isEmpty()){
                    val localItems =loadOfflineItems(siteFolder)

                    if (localItems.isNotEmpty()) {
                        _state.postValue(VideoNavState(
                            currentPage = VideoPage(
                                items = localItems,
                                title = "Offline - Downloads",
                                sourceId = siteItem.toString()
                            ),
                            history = emptyList(),
                            isLoading = false
                        ))
                    }
                }
                nextPageUrl=result.nextPageUrl
                val state=VideoNavState(
                    currentPage = VideoPage(
                        items = result.items.map { it.copy(siteManager = sitesManager) },
                        title = "Home",
                        sourceId = siteItem.toString()
                    ),
                    history =emptyList(),
                    isLoading =false
                )
                _state.postValue(state)
                isLoading.postValue(false)


            }
        }

    }
    fun loadOfflineItems(siteFolder: String):List<VideoItem>{

    }


    fun onItemClick(item: VideoItem) {
        isLoading.value=true
        val currentState = _state.value ?: run {
            return
        }

        // push current page into history
        val newHistory = currentState.history + currentState.currentPage
        viewModelScope.launch(Dispatchers.IO){
            val result=sitesManager.getPage(item.pageUrl!!)
            val newState= VideoNavState(
                currentPage = VideoPage(
                    items = result.items.map { it.copy(siteManager = sitesManager) },
                    title = item.title,
                    sourceId = item.pageUrl
                ),
                history =newHistory,
                isLoading = false
            )
            _state.postValue(newState)
            isLoading.postValue(false)

        }
    }

    fun goBack(): Boolean {
        val currentState = _state.value ?: run {
            return false
        }
        if (currentState.history.isEmpty()) {
            return false
        }

        val previousPage = currentState.history.last()
        val newHistory = currentState.history.dropLast(1)

        val newState= VideoNavState(
            currentPage = previousPage,
            history =newHistory,
            isLoading = false
        )
        _state.postValue(newState)

        return true
    }


}