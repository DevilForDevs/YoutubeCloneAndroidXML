package com.ranjan.expertclient.screens.bottomnavscreens.homescreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.HomeScreenBinding
import com.ranjan.expertclient.models.VideoItem
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.categoryrow.CategoryRowAdapter
import com.ranjan.expertclient.screens.bottomnavscreens.homescreen.widgets.videoscolumn.VideosColumnAdapter
import com.ranjan.expertclient.screens.browserscreen.Store
import com.ranjan.expertclient.screens.playerscreen.SharedVideoViewModel

class HomeScreen : Fragment() {

    private lateinit var binding: HomeScreenBinding
    private val viewModel by activityViewModels<Store>()
    private val sharedViewModel by activityViewModels<SharedVideoViewModel>()

    private lateinit var concatAdapter: ConcatAdapter
    val videosAdapter = VideosColumnAdapter(::onItemClick)

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
            videosAdapter.submitList(videos)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingIndicator.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }

        val categoryRowAdapter = CategoryRowAdapter(
            listOf("All","Music", "Movie", "Sports", "News","Live","Podcasts")
        )



        concatAdapter = ConcatAdapter(
            categoryRowAdapter,
            videosAdapter
        )

        binding.homeRecycler.layoutManager =
            LinearLayoutManager(requireContext())

        binding.homeRecycler.adapter = concatAdapter

        binding.homeRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                // trigger when last item is visible
                if (lastVisibleItem >= totalItemCount - 2) {
                    viewModel.handleWebFeedMore()
                }
            }
        })

        binding.topbar.searchIcon.setOnClickListener {
            val parentNavController = requireActivity()
                .findNavController(R.id.shoppingHostFragment)
            parentNavController.navigate(R.id.action_bottomNavScreen_to_searchScreen)
        }
    }
    fun onItemClick(item: VideoItem){
        sharedViewModel.selectedVideo.value=item
        val parentNavController = requireActivity()
            .findNavController(R.id.shoppingHostFragment)
        parentNavController.navigate(R.id.action_bottomNavScreen_to_playerScreen)

    }
}