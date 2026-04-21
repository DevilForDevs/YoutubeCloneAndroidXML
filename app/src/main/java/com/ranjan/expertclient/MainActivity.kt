package com.ranjan.expertclient

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ranjan.expertclient.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private var receivedUris: List<String> = emptyList()
    private var receivedFileNames: List<String?> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent) {
        val uris = extractIncomingUris(intent)
        if (uris.isEmpty()) return

        receivedUris = uris.map { it.toString() }
        receivedFileNames = uris.map { queryDisplayName(it) }

        Log.d(TAG, "Intent action: ${intent.action}")
        Log.d(TAG, "Intent type: ${intent.type}")
        Log.d(TAG, "Received Uris: $receivedUris")
        Log.d(TAG, "Received file names: $receivedFileNames")
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