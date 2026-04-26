package com.ranjan.expertclient.screens.sitesscreen


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ranjan.expertclient.utils.getOkHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Request
import org.json.JSONArray
import java.io.File
import java.io.IOException

class SitesScreenViewModel : ViewModel() {
    private val client = getOkHttpClient()

    private val _sitesList = MutableLiveData<MutableList<SiteItem>>()
    val sitesList: LiveData<MutableList<SiteItem>> = _sitesList
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun loadSites(context: Context) {
        _loading.postValue(true)
        _sitesList.postValue(mutableListOf())
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheFile = File(context.filesDir, "SupportedSites.json")
                val jsonBody = if (cacheFile.exists()) {
                    cacheFile.readText()
                } else {
                    val fetchedJson = fetchSitesJson()
                    cacheFile.writeText(fetchedJson)
                    fetchedJson
                }

                _sitesList.postValue(parseSites(jsonBody))
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.postValue(false)
            }
        }
    }

    private fun fetchSitesJson(): String {
        val sitesUrl = "https://raw.githubusercontent.com/DevilForDevs/YoutubeCloneAndroidXML/master/SupportedSites.json"
        val request = Request.Builder()
            .url(sitesUrl)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Network error")
            return response.body?.string() ?: "[]"
        }
    }

    private fun parseSites(json: String): MutableList<SiteItem> {
        val jsonArray = JSONArray(json)
        val items = mutableListOf<SiteItem>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            items.add(
                SiteItem(
                    title = obj.getString("name"),
                    url = obj.getString("url")
                )
            )
        }
        return items
    }
}
