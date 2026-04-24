package com.ranjan.expertclient.screens.moviessitesclient.moviesfeeds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.ranjan.expertclient.R
import com.ranjan.expertclient.databinding.MoviesFeedsScreenBinding
import com.ranjan.expertclient.models.VideoItem
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
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!viewModel.goBack()) {
                    view.findNavController().popBackStack()
                }
            }
        })

        val helper= RecyclerHelper(
            binding = binding,
            lifecycleOwner = viewLifecycleOwner,
            viewModel =viewModel ,
            fragment = this,
            onItemClick = ::onMovieItemClick,
            sharedViewModel
        )
        helper.setup()
        sharedViewModel.selectedSite.observe(viewLifecycleOwner){item ->
            viewModel.loadRoot(item,this.requireContext())
        }
        viewModel.error.observe(viewLifecycleOwner){error->
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }

    }
    fun onMovieItemClick(item: VideoItem){
       if (item.category){
           viewModel.onItemClick(item,this.requireContext())
       }else{
           sharedViewModel.selectedVideo.postValue(item)
           findNavController().navigate(R.id.action_moviesFeedsScreen_to_playerScreen)

       }
    }

}