package com.ranjan.expertclient.screens.moviessitesclient.moviesfeeds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.ranjan.expertclient.databinding.MoviesFeedsScreenBinding
import com.ranjan.expertclient.screens.playerscreen.SharedVideoViewModel

class MoviesFeedsScreen : Fragment() {

    private lateinit var binding: MoviesFeedsScreenBinding
    private val viewModel by activityViewModels<MoviesScreenViewModel>()
    private val sharedViewModel by activityViewModels<SharedVideoViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MoviesFeedsScreenBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val helper= RecyclerHelper(
            binding = binding,
            lifecycleOwner = viewLifecycleOwner,
            viewModel =viewModel ,
            fragment = this
        )
        helper.setup()
        sharedViewModel.selectedSite.observe(viewLifecycleOwner){item ->
            viewModel.getFeeds(item)
        }

    }

}