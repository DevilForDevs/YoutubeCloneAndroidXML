package com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.categoryrow

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.HomescreenCategoryRowBinding
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.categoryrow.widgets.CategoryAdapter


class CategoryRowHolder(
    private val binding: HomescreenCategoryRowBinding
) : RecyclerView.ViewHolder(binding.root) {

    private val adapter = CategoryAdapter()

    init {

        binding.homeCategoryRecycler.layoutManager =
            LinearLayoutManager(
                binding.root.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        binding.homeCategoryRecycler.adapter = adapter
    }

    fun bind(categories: List<String>) {
        adapter.submitList(categories)
    }


}