package com.ranjan.expertclient.screens.ytscreens.searchscreen.widgets.suggestionsrecycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.ranjan.expertclient.databinding.SuggestionsItemBinding
import com.ranjan.expertclient.models.SuggestionItem

class SuggestionAdapter(
    private val onClick: (SuggestionItem) -> Unit
) : androidx.recyclerview.widget.ListAdapter<SuggestionItem, SuggestionItemsHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionItemsHolder {
        val binding = SuggestionsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SuggestionItemsHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestionItemsHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SuggestionItem>() {

        override fun areItemsTheSame(
            oldItem: SuggestionItem,
            newItem: SuggestionItem
        ): Boolean {
            // 🔑 change this if you have unique ID
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(
            oldItem: SuggestionItem,
            newItem: SuggestionItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}