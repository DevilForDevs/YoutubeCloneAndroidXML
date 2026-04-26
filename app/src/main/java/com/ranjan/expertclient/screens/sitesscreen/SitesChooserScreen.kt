package com.ranjan.expertclient.screens.sitesscreen

import android.os.Build
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
import com.ranjan.expertclient.screens.browserscreen.Store
import com.ranjan.expertclient.screens.playerscreen.SharedVideoViewModel

class SitesChooserScreen : Fragment() {

    private lateinit var binding: SitesChooserScreenBinding
    private val viewModel by activityViewModels<SitesScreenViewModel>()
    private val sharedViewModel by activityViewModels<SharedVideoViewModel>()
    private val browserStore by activityViewModels<Store>()

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
        ViewCompat.setOnApplyWindowInsetsListener(view) { rootView, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            rootView.setPadding(0, statusBarHeight, 0, 0)
            insets
        }
        val recyclerHelper = RecyclerHelper(
            binding = binding,
            viewModel = viewModel,
            lifecycleOwner = viewLifecycleOwner,
            ::onItemClick,
            ::onDomainClick,
            ::onSchemaClick
        )
        recyclerHelper.setup()
        viewModel.loadSites(requireContext())

        browserStore.sitesUpdatedVersion.observe(viewLifecycleOwner) { version ->
            if (version > 0L) {
                viewModel.loadSites(requireContext())
            }
        }
    }

    fun onItemClick(item: SiteItem) {
        sharedViewModel.selectedSite.postValue(item)
        println(item)
        if (item.url.contains("youtube")) {
            browserStore.chooseDomain.value = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                findNavController().navigate(R.id.action_sitesChooserScreen_to_browserScreen)
            } else {
                findNavController().navigate(R.id.action_sitesChooserScreen_to_bottomNavScreen)
            }
        }else{
            findNavController().navigate(R.id.action_sitesChooserScreen_to_moviesFeedsScreen)
        }
    }

    fun onDomainClick(item: SiteItem) {
        sharedViewModel.selectedSite.postValue(item)
        browserStore.setChoosingDomain(item.title)
        findNavController().navigate(R.id.action_sitesChooserScreen_to_browserScreen)
    }

    fun onSchemaClick(item: SiteItem) {
    }

}