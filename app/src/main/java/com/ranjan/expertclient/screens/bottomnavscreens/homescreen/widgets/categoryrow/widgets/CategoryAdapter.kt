package com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.categoryrow.widgets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.ranjan.expertclient.databinding.HomeScreenCategoryItemBinding

class CategoryAdapter :
    ListAdapter<String, CategoryHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        val binding = HomeScreenCategoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        holder.bind(getItem(position))
    }
}