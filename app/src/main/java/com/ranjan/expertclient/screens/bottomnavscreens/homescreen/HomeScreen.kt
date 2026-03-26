package com.ranjan.expertclient.screens.bottomnavscreens.homescreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.HomeScreenBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.categoryrow.CategoryRowAdapter
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.VideosColumnAdapter
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.VideosColumnHolder
import com.ranjan.expertclient.screens.browserscreen.Store
import kotlin.getValue

class HomeScreen : Fragment() {

    private lateinit var binding: HomeScreenBinding
    private val viewModel by activityViewModels<Store>()


    private lateinit var concatAdapter: ConcatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = HomeScreenBinding.inflate(inflater, container, false)


        setupRecycler()

        return binding.root
    }

    private fun setupRecycler() {

        viewModel.webFeedsData.observe(viewLifecycleOwner){videos->
            val holder = binding.homeRecycler.findViewHolderForAdapterPosition(0) as? VideosColumnHolder
            holder?.bind(videos)
        }

        val categoryRowAdapter = CategoryRowAdapter(
            listOf("All","Music", "Movie", "Sports", "News","Live","Podcasts")
        )

        val videosAdapter = VideosColumnAdapter(viewModel.webFeedsData.value?:emptyList())

        concatAdapter = ConcatAdapter(
            categoryRowAdapter,
            videosAdapter
        )

        binding.homeRecycler.layoutManager =
            LinearLayoutManager(requireContext())

        binding.homeRecycler.adapter = concatAdapter

        binding.topbar.searchIcon.setOnClickListener {
            val parentNavController = requireActivity()
                .findNavController(R.id.shoppingHostFragment)
            parentNavController.navigate(R.id.action_bottomNavScreen_to_searchScreen)
        }
    }
}