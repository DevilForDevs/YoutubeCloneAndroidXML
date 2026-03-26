package com.ranjan.expertclient.screens.browserscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.browserscreen.parsers.parseInitialData
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import org.json.JSONArray
import org.json.JSONObject

class Store: ViewModel() {
    private val webFeeds = MutableLiveData<MutableList<VideoItem>>(mutableListOf())
    val webFeedsData: LiveData<MutableList<VideoItem>> get() = webFeeds

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

}







