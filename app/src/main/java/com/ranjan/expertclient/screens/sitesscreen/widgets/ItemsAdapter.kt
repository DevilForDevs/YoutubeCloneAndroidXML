package com.ranjan.expertclient.screens.sitesscreen.widgets
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.databinding.SiteItemBinding
import com.ranjan.expertclient.screens.sitesscreen.SiteItem

class ItemsAdapter(
    private val items: List<SiteItem>,
    private val onItemClick: (item: SiteItem) -> Unit,
    private val onDomainClick:(item: SiteItem)-> Unit,
    private val onSchemaClick:(item: SiteItem)-> Unit,
) : RecyclerView.Adapter<ItemsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsHolder {
        val binding = SiteItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemsHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemsHolder, position: Int) {
        holder.bind(items[position],onItemClick,onDomainClick,onSchemaClick)
    }

    override fun getItemCount() = items.size

}
