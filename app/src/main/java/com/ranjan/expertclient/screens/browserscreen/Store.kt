package com.ranjan.expertclient.screens.browserscreen

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.browserscreen.endpoints.HomePageBrowse
import com.ranjan.expertclient.screens.browserscreen.parsers.parseInitialData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class Store : ViewModel() {
    private val webFeeds = MutableLiveData<MutableList<VideoItem>>(mutableListOf())
    val webFeedsData: LiveData<MutableList<VideoItem>> get() = webFeeds
    val chooseDomain = MutableLiveData<String?>()
    private val _currentUrl = MutableLiveData<String>("")
    val currentUrl: LiveData<String> = _currentUrl
    private val _selectedDomain = MutableLiveData<String>("")
    val selectedDomain: LiveData<String> = _selectedDomain

    private val _navigateBack = MutableLiveData(false)
    val navigateBack: LiveData<Boolean> = _navigateBack
    private val _sitesUpdatedVersion = MutableLiveData(0L)
    val sitesUpdatedVersion: LiveData<Long> = _sitesUpdatedVersion

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private var isRequestInFlight = false
    private val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 15; CPH2665 Build/AP3A.240617.008; wv) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 " +
            "Chrome/143.0.7499.34 Mobile Safari/537.36"

    var mWebClientVersion: String? = null
    var visitorId: String? = null
    var feedType = "mwebfeeds"
    var continuation: String? = null

    fun setChoosingDomain(websiteTitle: String) {
        chooseDomain.postValue(websiteTitle)
        if (chooseDomain.value != null) {
            _currentUrl.postValue("")
            _selectedDomain.postValue("")
        }
    }

    fun updateCurrentUrl(url: String?) {
        if (url.isNullOrBlank()) return
        _currentUrl.postValue(url)
    }

    fun chooseCurrentDomain(context: Context) {
        val selectedUrl = _currentUrl.value.orEmpty()
        val websiteTitle = chooseDomain.value.orEmpty()
        if (selectedUrl.isBlank()) {
            Log.d("Store", "No URL selected yet for domain choice")
            println("No URL selected yet for domain choice")
            return
        }
        if (websiteTitle.isBlank()) {
            Log.d("Store", "No website title set for domain choice")
            println("No website title set for domain choice")
            return
        }

        val domain = selectedUrl.toUri().host.orEmpty()
        _selectedDomain.postValue(domain)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheFile = File(context.filesDir, "SupportedSites.json")
                val jsonBody = if (cacheFile.exists()) cacheFile.readText() else "[]"
                val sitesArray = JSONArray(jsonBody)

                var updated = false
                for (i in 0 until sitesArray.length()) {
                    val obj = sitesArray.getJSONObject(i)
                    if (obj.optString("name").equals(websiteTitle, ignoreCase = true)) {
                        obj.put("url", selectedUrl)
                        updated = true
                        break
                    }
                }

                if (!updated) {
                    val newSite = JSONObject().apply {
                        put("name", websiteTitle)
                        put("url", selectedUrl)
                        put("schemaUrl", "")
                    }
                    sitesArray.put(newSite)
                }

                cacheFile.writeText(sitesArray.toString(2))
                _sitesUpdatedVersion.postValue(System.currentTimeMillis())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val message = "Selected URL: $selectedUrl | Selected domain: $domain"
        Log.d("Store", message)
        println(message)

        chooseDomain.postValue(null)
        _navigateBack.postValue(true)
    }

    fun onBackNavigationHandled() {
        _navigateBack.value = false
    }

    fun handleWebFeed(jsonString: String, taskCompleted: () -> Unit) {
        if (webFeeds.value?.isEmpty() ?: true) {
            val finalJson = JSONObject(jsonString)
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
                val result = parseInitialData(responseContext, "mwebfeeds")
                webFeeds.postValue(result.first)
                continuation = result.second
                if (result.first.isNotEmpty()) {
                    taskCompleted()
                }
            }
        }
    }

    fun handleWebFeedMore() {
        val cont = continuation ?: return
        val visitor = visitorId ?: return
        val clientVersion = mWebClientVersion ?: return

        if (isRequestInFlight) return

        isRequestInFlight = true
        _isLoading.postValue(true)

        val browser = HomePageBrowse(clientVersion, USER_AGENT)
        val templist = webFeeds.value

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = browser.continueBrowse(cont, visitor)
                val result = parseInitialData(response, "mwebcontinuation")
                if (templist != null) {
                    webFeeds.postValue((templist + result.first) as MutableList<VideoItem>?)
                    continuation = result.second
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isRequestInFlight = false
                _isLoading.postValue(false)
            }
        }
    }
}
