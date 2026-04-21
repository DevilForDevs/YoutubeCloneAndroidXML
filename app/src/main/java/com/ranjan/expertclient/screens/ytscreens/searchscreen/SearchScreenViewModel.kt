package com.ranjan.expertclient.screens.ytscreens.searchscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.browserscreen.parsers.parseInitialData
import com.ranjan.expertclient.screens.ytscreens.searchscreen.YtSearchFetcher
import com.ranjan.expertclient.screens.ytscreens.searchscreen.parsers.createVideoTree
import com.ranjan.expertclient.screens.ytscreens.searchscreen.parsers.parseSearchYt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class SearchScreenViewModel: ViewModel() {
    private val searchItems = MutableLiveData<MutableList<VideoItem>>(mutableListOf())
    val search_items: LiveData<MutableList<VideoItem>> get() = searchItems
    val _isLoading = MutableLiveData<Boolean>()
    var continuation: String? =""
    var _query=""
    var inflight=false

    fun getInitialResult(query: String){
        _isLoading.value=true
        _query=query
        viewModelScope.launch(Dispatchers.IO){
            val ytJson= YtSearchFetcher.fetch(query,null,null)
            val result= parseSearchYt(JSONObject(ytJson),"ytsearch")
            searchItems.postValue(result.first)
            continuation=result.second
            _isLoading.postValue(false)
        }
    }
    fun loadMore(){

        if (inflight||_isLoading.value==true) return;
        inflight=true
        _isLoading.value=true
        if (_query==""||continuation=="")return
        viewModelScope.launch(Dispatchers.IO){
            val ytJson= YtSearchFetcher.fetch(_query,continuation,null)
            val result= parseSearchYt(JSONObject(ytJson),"ytsearch_continuation")
            val oldItems=searchItems.value
            if (oldItems != null) {
                searchItems.postValue((oldItems+result.first) as MutableList<VideoItem>?)
            }
            continuation=result.second
            _isLoading.postValue(false)
            inflight=false
        }
    }
}