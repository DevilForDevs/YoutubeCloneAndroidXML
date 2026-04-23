package com.ranjan.expertclient.screens.sitesscreen


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException

class SitesScreenViewModel : ViewModel() {
    private val client by lazy { OkHttpClient() }

    private val _sitesList = MutableLiveData<MutableList<SiteItem>>()
    val sitesList: LiveData<MutableList<SiteItem>> = _sitesList
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    fun loadSites() {
        _loading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://studyzem.com/sites.json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Network error")

                    val body = response.body?.string() ?: ""
                    val jsonArray = JSONArray(body)
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
                    _sitesList.postValue(items)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.postValue(false)
            }
        }
    }
}