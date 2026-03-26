package com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.categoryrow.widgets

import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.HomeScreenCategoryItemBinding

class CategoryHolder(
    private val binding: HomeScreenCategoryItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(text: String) {
        binding.textView2.text = text
    }
}