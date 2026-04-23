package com.ranjan.expertclient.screens.sitesscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.SitesChooserScreenBinding
import com.ranjan.expertclient.screens.playerscreen.SharedVideoViewModel

class SitesChooserScreen : Fragment() {

    private lateinit var binding: SitesChooserScreenBinding
    private val viewModel by activityViewModels<SitesScreenViewModel>()
    private val sharedViewModel by activityViewModels<SharedVideoViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SitesChooserScreenBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
        val recyclerHelper= RecyclerHelper(
            binding = binding,
            viewModel = viewModel,
            lifecycleOwner = viewLifecycleOwner,
            ::onItemClick
        )
        recyclerHelper.setup()
        viewModel.loadSites(requireContext())
    }
    fun onItemClick(item: SiteItem){
        sharedViewModel.selectedSite.postValue(item)
        if (item.url.contains("youtube")){
            findNavController().navigate(R.id.action_sitesChooserScreen_to_browserScreen)
        }
        if (item.url.contains("mp4moviez")){
            findNavController().navigate(R.id.action_sitesChooserScreen_to_moviesFeedsScreen)

        }
    }

}