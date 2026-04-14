package com.ranjan.expertclient.screens.playerscreen.widgets.resolution

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.ItemResolutionBinding

class ResolutionAdapter(
    private val onSelect: (String) -> Unit
) : RecyclerView.Adapter<ResolutionAdapter.ViewHolder>() {

    private val items = mutableListOf<String>()
    private var selectedItem: String? = null

    fun submitList(list: List<String>, current: String?) {
        items.clear()
        items.addAll(list)
        selectedItem = current
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemResolutionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemResolutionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.resolutionLabel.text = item
        holder.binding.checkIcon.visibility =
            if (item == selectedItem) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener {
            onSelect(item)
        }
    }
}