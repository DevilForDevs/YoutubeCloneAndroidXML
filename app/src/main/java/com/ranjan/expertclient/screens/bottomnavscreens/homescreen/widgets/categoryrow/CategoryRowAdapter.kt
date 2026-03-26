package com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.categoryrow
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.HomescreenCategoryRowBinding

class CategoryRowAdapter(
    private val categories: List<String>
) : RecyclerView.Adapter<CategoryRowHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryRowHolder {
        val binding = HomescreenCategoryRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryRowHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryRowHolder, position: Int) {
        holder.bind(categories)
    }

    override fun getItemCount(): Int = 1
}