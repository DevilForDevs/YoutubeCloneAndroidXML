package com.ranjan.expertclient.screens.searchscreen.widgets.suggestionsrecycler

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ranjan.expertclient.databinding.SuggestionsItemBinding
import com.ranjan.expertclient.models.SuggestionItem

class SuggestionItemsHolder (
    private val binding: SuggestionsItemBinding
): RecyclerView.ViewHolder(binding.root){
    fun bind(item: SuggestionItem){
        binding.textView6.text=item.title
        if(item.thumbnail!=null){
            Glide.with(binding.imageView10)
                .load(item.thumbnail)
                .into(binding.imageView10)
        }


    }

}