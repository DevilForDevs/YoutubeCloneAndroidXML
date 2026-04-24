package com.ranjan.expertclient.screens.playerscreen.widgets.download


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.ranjan.expertclient.databinding.DownloadItemBinding
import com.ranjan.expertclient.screens.playerscreen.models.DownloadItem

class DownloadsAdapter(
    private val onActionClick: (DownloadItem) -> Unit,
    private val play:(item:DownloadItem)->Unit
) : ListAdapter<DownloadItem, DownloadItemHolder>(DownloadDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadItemHolder {
        val binding = DownloadItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DownloadItemHolder(binding)
    }

    override fun onBindViewHolder(holder: DownloadItemHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item,onActionClick,play)
    }

    companion object DownloadDiffCallback : DiffUtil.ItemCallback<DownloadItem>() {
        override fun areItemsTheSame(oldItem: DownloadItem, newItem: DownloadItem): Boolean {
            return oldItem.resolution == newItem.resolution
        }

        override fun areContentsTheSame(oldItem: DownloadItem, newItem: DownloadItem): Boolean {
            return oldItem == newItem
        }
    }
}

