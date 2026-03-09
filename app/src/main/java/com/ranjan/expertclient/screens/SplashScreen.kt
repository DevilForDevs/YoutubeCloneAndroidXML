package com.ranjan.expertclient.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ranjan.expertclient.databinding.SplashScreenBinding
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.io.MemoryUsageSetting.setupTempFileOnly
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

@SuppressLint("CustomSplashScreen")
class SplashScreen : Fragment() {
    private lateinit var binding: SplashScreenBinding
    private val backThread = CoroutineScope(Dispatchers.IO)
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SplashScreenBinding.inflate(inflater, container, false)

        // Initialize PDFBox Android
        PDFBoxResourceLoader.init(requireContext())

        val requiredUrl = "https://epaper.prabhatkhabar.com/api/published-editions/slug/patna-city/2026-03-09"

       binding.btnStartMerge.setOnClickListener {
           backThread.launch {
               val pdfUrls = handlePrabhatKhabar(requiredUrl)
               println("PDF URLs: $pdfUrls")

               if (pdfUrls.isNotEmpty()) {
                   mergePdfUrls(pdfUrls)
               }
           }
       }

        return binding.root
    }

    fun handlePrabhatKhabar(url: String): List<String> {
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return emptyList()

        val pdfUrls = mutableListOf<String>()
        val json = JSONObject(body)
        val pages = json.getJSONObject("data").getJSONArray("pages")

        for (i in 0 until pages.length()) {
            val page = pages.getJSONObject(i)
            if (page.has("currentImage")) {
                val currentImage = page.getJSONObject("currentImage")
                if (currentImage.has("pdf")) {
                    val pdfPath = currentImage.getString("pdf")
                    pdfUrls.add("https://cdnimg.prabhatkhabar.com/pdf/$pdfPath")
                }
            }
        }

        return pdfUrls
    }

    private fun mergePdfUrls(pdfUrls: List<String>) {
        try {
            val outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (!outputDir.exists()) outputDir.mkdirs()  // create if
            val mergedFile = File(outputDir, "merged.pdf")

            val merger = PDFMergerUtility()
            merger.destinationFileName = mergedFile.absolutePath

            val tempFiles = mutableListOf<File>()

            // Download PDFs and save to temporary files
            pdfUrls.forEachIndexed { index, url ->
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    val bytes = response.body?.bytes() ?: return@forEachIndexed
                    val tempFile = File.createTempFile("temp_$index", ".pdf", requireContext().cacheDir)
                    FileOutputStream(tempFile).use { it.write(bytes) }
                    tempFiles.add(tempFile)
                    backThread.launch(Dispatchers.Main){
                        binding.tvProgress.text="Progress ${index+1}/${pdfUrls.size}"
                    }
                    merger.addSource(tempFile)
                }
            }


            backThread.launch(Dispatchers.Main){
                binding.tvProgress.text="Merging ..."
            }
            // Merge PDFs using disk-based scratch to prevent OOM
            merger.mergeDocuments(setupTempFileOnly())
            backThread.launch(Dispatchers.Main){
                binding.tvProgress.text="Merged PDF saved at"
            }
            println("Merged PDF saved at: ${mergedFile.absolutePath}")
            // Clean up temporary files
            tempFiles.forEach { it.delete() }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}