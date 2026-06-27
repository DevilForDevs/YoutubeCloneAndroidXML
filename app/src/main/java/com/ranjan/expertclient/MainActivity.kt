package com.ranjan.expertclient

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.ranjan.expertclient.databinding.ActivityMainBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.playerscreen.SharedVideoViewModel
import com.ranjan.expertclient.utils.extractVideoId
import kotlin.getValue


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private var receivedUris: List<String> = emptyList()
    private var receivedFileNames: List<String?> = emptyList()
    private val sharedViewModel: SharedVideoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        handleIncomingIntent(intent)
    }

    private fun getNavController() =
        (supportFragmentManager.findFragmentById(R.id.shoppingHostFragment)
                as NavHostFragment).navController

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent) {
        Log.d(TAG, "Intent action: ${intent.action}")
        Log.d(TAG, "Intent type: ${intent.type}")

        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {

            val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return

            val url = extractFirstUrl(text)
            if (url!=null){
                val videoId= extractVideoId(url)
                if (videoId!=null){
                    val videoItem= VideoItem(
                        videoId=videoId,
                        title = "From Intent"
                    )
                    sharedViewModel.selectedVideo.value=videoItem
                }

            }

            return
        }

        val uris = extractIncomingUris(intent)
        if (uris.isEmpty()) return

        receivedUris = uris.map { it.toString() }
        receivedFileNames = uris.map { queryDisplayName(it) }

        Log.d(TAG, "Received Uris: $receivedUris")
    }
    private fun extractFirstUrl(text: String): String? {
        val regex = Regex("""https?://[^\s]+""")
        return regex.find(text)?.value
    }

    private fun extractIncomingUris(intent: Intent): List<Uri> {
        return when (intent.action) {
            Intent.ACTION_SEND -> extractSingleUri(intent)
            Intent.ACTION_SEND_MULTIPLE -> extractMultipleUris(intent)
            else -> intent.data?.let { listOf(it) }.orEmpty()
        }
    }

    private fun extractSingleUri(intent: Intent): List<Uri> {
        val stream = @Suppress("DEPRECATION") intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        if (stream != null) return listOf(stream)

        return intent.clipData?.let { clipData ->
            buildList {
                for (i in 0 until clipData.itemCount) {
                    clipData.getItemAt(i).uri?.let { add(it) }
                }
            }
        }.orEmpty()
    }

    private fun extractMultipleUris(intent: Intent): List<Uri> {
        @Suppress("DEPRECATION")
        val streams = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
        if (!streams.isNullOrEmpty()) return streams

        return intent.clipData?.let { clipData ->
            buildList {
                for (i in 0 until clipData.itemCount) {
                    clipData.getItemAt(i).uri?.let { add(it) }
                }
            }
        }.orEmpty()
    }

    private fun queryDisplayName(uri: Uri): String? {
        return if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else {
                    uri.lastPathSegment
                }
            }
        } else {
            uri.lastPathSegment
        }
    }
}