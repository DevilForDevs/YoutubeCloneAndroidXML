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
        println(item)
        binding.resolutionText.text = item.resolution
        binding.sizeText.text = item.dbyBydt

        binding.downloadProgress.progress = item.progressPercent
        binding.percentText.text = "${item.progressPercent}%"

        binding.downloadStatus.text = item.status

        if (item.isFinished) {
            binding.actionButton.visibility = View.GONE
            binding.downloadProgress.progress = 100
            binding.percentText.text = "100%"
        } else {
            binding.actionButton.visibility = View.VISIBLE
            if (item.isDownloading) {
                binding.actionButton.setImageResource(android.R.drawable.ic_media_pause)
            } else {
                binding.actionButton.setImageResource(android.R.drawable.ic_media_play)
            }
        }

        binding.actionButton.setOnClickListener {
            if (!item.isFinished) {
                action(item)
            }
        }
        binding.root.setOnClickListener {
            play(item)
        }
    }

}
