package com.ranjan.expertclient.screens.sitesscreen

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.ranjan.expertclient.databinding.SitesChooserScreenBinding
import com.ranjan.expertclient.screens.sitesscreen.widgets.ItemsAdapter

class RecyclerHelper(
    private val binding: SitesChooserScreenBinding,
    private val viewModel: SitesScreenViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val onItemClick: (item: SiteItem) -> Unit,
    private val onDomainClick:(item: SiteItem)-> Unit,
    private val onSchemaClick:(item: SiteItem)-> Unit,

    ) {
    fun setup(){
        binding.siteRecycler.layoutManager = LinearLayoutManager(binding.root.context)
        viewModel.sitesList.observe(lifecycleOwner){items->
            binding.siteRecycler.apply {
                adapter = ItemsAdapter(items,onItemClick,onDomainClick,onSchemaClick)
            }
        }
        viewModel.loading.observe(lifecycleOwner){isLoading->
            binding.progressBar4.visibility=if (isLoading) View.VISIBLE else View.GONE
        }
    }




}