package com.ranjan.expertclient.screens.commanscreens.feeds_screen


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.moviesitesxtractors.SitesManager
import com.ranjan.expertclient.screens.sitesscreen.SiteItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class FeedsScreenViewModel : ViewModel() {
    private val sitesManager = SitesManager()
    private val _feedsList = MutableLiveData<MutableList<VideoItem>>()
    val feedsList: LiveData<MutableList<VideoItem>> = _feedsList
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    var currentSite: SiteItem?=null

    fun loadingFeeds(siteItem: SiteItem,context: Context){
        if (currentSite!=null){
            return
        }
       _loading.postValue(true)
        currentSite=siteItem
        viewModelScope.launch(Dispatchers.IO){
            val sf= File(context.filesDir,siteItem.title )
            if (!sf.exists()){
                sf.mkdir()
            }
            val siteFolder=sf.absolutePath
            val result=sitesManager.getFeeds(siteItem, siteFolder)
        }
    }
}

//https://vidneo.org/wp-content/uploads/2026/06/Jennifer_&_Rudra_Pratap_Uncut.mp4
//https://vidneo.org/wp-content/uploads/2026/06/Aakhri_Sukh_Hot_Scenes.mp4
//https://vidneo.org/wp-content/uploads/2025/03/Hot_Hindi_Web_Series_Best_Scene_24.mp4
//https://vidneo.org/wp-content/uploads/2026/06/Yaaddasht_E1_2026.mp4
//https://vidneo.org/wp-content/uploads/2026/06/Janvi_Hot_Uncut_Series.mp4
//https://vidneo.org/wp-content/uploads/2025/02/Kaamwali_E4_PrimeShots.mp4
//https://vidneo.org/wp-content/uploads/2026/06/Bharya_E2.mp4
//https://cdn.4vidz.com/Hotx/Karma+2021+HotX+Originals.mp4
//https://video.maalcdn.com/Tri%20Flicks/Tharki%20Naukar/Tharki%20Naukar%20E2_1.mp4?X-Amz-Algorithm=AWS4-HMAC-SHA256&#038;X-Amz-Credential=809a68eb5f9a4d13e1b8b283b1fce3fe%2F20260611%2Fauto%2Fs3%2Faws4_request&#038;X-Amz-Date=20260611T162054Z&#038;X-Amz-Expires=604800&#038;X-Amz-SignedHeaders=host&#038;X-Amz-Signature=e6eb7fa0e26a36e2ae559062db18d4dcf84fb5c592fd43bfa93d64178a7d1f49