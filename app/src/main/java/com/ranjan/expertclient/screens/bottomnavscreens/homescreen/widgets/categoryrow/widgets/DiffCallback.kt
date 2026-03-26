package com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.categoryrow.widgets

import androidx.recyclerview.widget.DiffUtil

class DiffCallback : DiffUtil.ItemCallback<String>() {

    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}