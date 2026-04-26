package com.ranjan.expertclient.screens.sitesscreen.widgets

import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.SiteItemBinding
import com.ranjan.expertclient.screens.sitesscreen.SiteItem


class ItemsHolder(
    private val binding: SiteItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        item: SiteItem,
        onItemClick:(item: SiteItem)-> Unit,
        onDomainClick:(item: SiteItem)-> Unit,
        onSchemaClick:(item: SiteItem)-> Unit,
    ) {
        binding.domain.setOnClickListener {
            onDomainClick(item)
        }
        binding.root.setOnClickListener {
            onItemClick(item)
        }
        binding.schema.setOnClickListener {
            onSchemaClick(item)
        }
        binding.textView41.text = item.title

    }

}