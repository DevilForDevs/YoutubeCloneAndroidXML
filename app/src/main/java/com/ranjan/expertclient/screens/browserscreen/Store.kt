package com.ranjan.expertclient.screens.browserscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.browserscreen.endpoints.HomePageBrowse
import com.ranjan.expertclient.screens.browserscreen.parsers.parseInitialData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import org.json.JSONArray
import org.json.JSONObject

class Store: ViewModel() {
    private val webFeeds = MutableLiveData<MutableList<VideoItem>>(mutableListOf())
    val webFeedsData: LiveData<MutableList<VideoItem>> get() = webFeeds

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private var isRequestInFlight = false
    private  val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 15; CPH2665 Build/AP3A.240617.008; wv) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 " +
                "Chrome/143.0.7499.34 Mobile Safari/537.36"

    var mWebClientVersion: String?=null
    var visitorId: String?=null
    var feedType="mwebfeeds"
    var continuation: String?=null
    fun handleWebFeed(jsonString: String,taskCompleted:()-> Unit){
        if (webFeeds.value.isEmpty()){
            val finalJson = JSONObject(jsonString)
            if (finalJson.has("url")){
                if (finalJson.getString("url").contains("search?prettyPrint")){
                    val responseContext=finalJson.getJSONObject("data")
                    visitorId  = safeGet(
                        responseContext,
                        listOf("responseContext", "webResponseContextExtensionData", "ytConfigData", "visitorData")
                    ) as String?

                    mWebClientVersion= safeGet(responseContext,
                        listOf("responseContext","serviceTrackingParams",0,"params",2,"value")) as String?
                    val result= parseInitialData(responseContext,"mwebsearch")
                    webFeeds.postValue(result.first)
                    continuation=result.second
                    feedType="mwebsearch"
                    taskCompleted()

                }
            }
            if (finalJson.has("data")) {
                val responseContext = finalJson.getJSONObject("data")
                visitorId = safeGet(
                    responseContext,
                    listOf(
                        "responseContext",
                        "webResponseContextExtensionData",
                        "ytConfigData",
                        "visitorData"
                    )
                ) as String?

                mWebClientVersion = safeGet(
                    responseContext,
                    listOf("responseContext", "serviceTrackingParams", 0, "params", 2, "value")
                ) as String?
                val result= parseInitialData(responseContext,"mwebfeeds")
                webFeeds.postValue(result.first)
                continuation=result.second
                taskCompleted()

            }
        }
    }

    fun handleWebFeedMore() {
        val cont = continuation ?: return
        val visitor = visitorId ?: return
        val clientVersion = mWebClientVersion ?: return

        if (isRequestInFlight) return   // ✅ instant check

        isRequestInFlight = true       // ✅ block immediately
        _isLoading.postValue(true)

        val browser = HomePageBrowse(clientVersion, USER_AGENT)
        val templist=webFeeds.value

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = browser.continueBrowse(cont, visitor)
                val result = parseInitialData(response, "mwebcontinuation")
                if (templist!=null){
                    webFeeds.postValue((templist+result.first) as MutableList<VideoItem>?)
                    continuation = result.second
                }


            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isRequestInFlight = false   // ✅ release lock
                _isLoading.postValue(false)
            }
        }
    }


}







