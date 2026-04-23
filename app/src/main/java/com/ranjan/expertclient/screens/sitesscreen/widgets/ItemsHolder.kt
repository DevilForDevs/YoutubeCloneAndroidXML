package com.ranjan.expertclient.screens.sitesscreen.widgets

import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.SiteItemBinding
import com.ranjan.expertclient.screens.sitesscreen.SiteItem


class ItemsHolder(
    private val binding: SiteItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SiteItem,onItemClick:(item: SiteItem)-> Unit) {
        binding.textView41.text = item.title
        binding.root.setOnClickListener {
            onItemClick(item)
        }
    }

}