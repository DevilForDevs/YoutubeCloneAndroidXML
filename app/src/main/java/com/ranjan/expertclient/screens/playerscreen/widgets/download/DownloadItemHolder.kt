package com.ranjan.expertclient.screens.playerscreen.widgets.download

import android.view.View
import com.ranjan.expertclient.databinding.DownloadItemBinding
import com.ranjan.expertclient.screens.playerscreen.models.DownloadItem
import com.ranjan.expertclient.utils.convertBytes

import androidx.recyclerview.widget.RecyclerView


class DownloadItemHolder(
    private val binding: DownloadItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: DownloadItem, action: (item: DownloadItem) -> Unit, play: (item: DownloadItem) -> Unit) {
        binding.resolutionText.text = item.resolution
        binding.sizeText.text = item.dbyBydt

        binding.downloadProgress.progress = item.progressPercent
        binding.percentText.text = "${item.progressPercent}%"

        if (item.isDownloading) {
            binding.actionButton.setImageResource(android.R.drawable.ic_media_pause)
        } else {
            binding.actionButton.setImageResource(android.R.drawable.ic_media_play)
        }

        binding.downloadStatus.text=item.status

        if (item.isFinished) {
            binding.actionButton.setImageResource(android.R.drawable.ic_media_play) // Or a different icon for local play
            binding.downloadProgress.progress = 100
            binding.percentText.text = "100%"
        }
        
        binding.downloadStatus.text = item.status
        
        binding.actionButton.setOnClickListener {
            action(item)
        }
        binding.root.setOnClickListener {
            play(item)
        }
    }

}
